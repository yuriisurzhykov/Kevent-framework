package com.github.yuriisurzhykov.kevent.activeobject.bus

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.communication.CollectionFlowCommunication
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.EventNotValidException
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.events.codegen.DefaultStickyEventsFactory
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEventRegistry
import com.github.yuriisurzhykov.kevent.events.validation.WrongEventKeyFormatException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

/**
 *  Interface representing a FlowBus, a bus system for managing and dispatching events. Using
 *  this `EventBus` you can subscribe your [ActiveObject] for set of events. Every event, that to
 *  be sent may be sticky or non-sticky(regular). Beside subscribing [ActiveObject] for events,
 *  you also can subscribe any other class for event. This might be helpful for UI layer and theirs
 *  `ViewModels`
 */
interface FlowBus : EventManager {

    /**
     *  Subscribes an active object to the bus.
     *
     *  @param activeObject The active object to be subscribed.
     */
    fun subscribe(activeObject: ActiveObject)

    /**
     *  Subscribes to a specific type of event on the bus.
     *
     *  @param T The type of event that extends from Event.
     *  @param eventClass The KClass of the event type to subscribe to.
     *  @param scope The CoroutineScope in which the events are observed.
     *  @param eventHandler A suspend function that is called on each event emission.
     */
    fun <T : Event> subscribe(
        eventClass: KClass<T>,
        scope: CoroutineScope,
        eventHandler: suspend (T) -> Unit
    )

    /**
     *  Abstract class that represents a flow bus for handling and dispatching events.
     *  It provides the functionality to send and subscribe to events. General purpose of this class
     *  is to subscribe [ActiveObject] to the [Event] through [SharedFlow].
     *  When you call [subscribe] function it creates new [SharedFlow], if it not exists for given
     *  [Event], and requests [ActiveObject] to subscribe to this flow.
     * */
    abstract class Abstract(
        private val stickyEventsFactory: DefaultStickyEventsFactory,
        private val flowCommunication: CollectionFlowCommunication,
        private val persistableEventRegistry: PersistableEventRegistry,
        private val eventValidator: EventValidator
    ) : FlowBus {

        /**
         * Implementation for [FlowBus.subscribe] function. Just triggers active object's subscribe
         * method to pass Kotlin SharedFlow instance of communication path.
         * */
        override fun subscribe(activeObject: ActiveObject) {
            activeObject.subscribeTo(flowCommunication.asSharedFlow())
        }

        /**
         * Subscribes to the given event class and registers an event handler to be called when an event of that class
         * is emitted. The event handler is a suspend function that takes the event as a parameter and does not return
         * any value.
         *
         * @param eventClass The class of the event to subscribe to.
         * @param scope The CoroutineScope to use when launching the subscription.
         * @param eventHandler The event handler function that will be called when an event is emitted.
         * @throws IllegalArgumentException if the eventClass is not a subtype of Event.
         * */
        override fun <T : Event> subscribe(
            eventClass: KClass<T>,
            scope: CoroutineScope,
            eventHandler: suspend (T) -> Unit
        ) {
            flowCommunication.asSharedFlow()
                .filterIsInstance(eventClass)
                .onEach { event -> eventHandler.invoke(event) }
                .launchIn(scope)
        }

        /**
         *  Sends an event to all subscribers of this event type. If the event is sticky, it will be
         *  stored and can be retrieved later.
         *
         *  If you override this function you have to call it's super.send() because it publishes
         *  events to their corresponding [SharedFlow].
         *
         *  @param event The event to be sent.
         *  @throws WrongEventKeyFormatException if the event key is not in valid values range
         *  @throws EventNotValidException If the event has validation logic and validation failed.
         * */
        override suspend fun <T : Event> publish(event: T) {
            if (event is Event.Validatable) {
                val error = event.validate(this)
                if (error != null) throw error
            }
            if (event is Event.KeyValidatable<*>) {
                eventValidator.validateOrThrow(event as Event.KeyValidatable<*>)
            }
            flowCommunication.emit(event)
            if (event is Event.Persistable) {
                persistableEventRegistry.persist(event)
            }
        }

        /**
         * Retrieves the last emitted sticky event of the specified type.
         *
         * @param clazz The class of the event.
         * @return The last emitted sticky event of the specified type or null if no such event was emitted.
         * */
        override suspend fun <E : Event.Sticky> getSticky(clazz: KClass<E>): E {
            return try {
                flowCommunication.getSticky(clazz)
            } catch (e: NoStickyValueFoundException) {
                val persistedStickyList = persistableEventRegistry.readPersisted(clazz)
                if (persistedStickyList.isEmpty()) {
                    stickyEventsFactory.createDefault(clazz)
                } else {
                    persistedStickyList.first()
                }
            }
        }

        /**
         * Obtains a sticky event which doesn't belongs to a specific partition and has only
         * one instance with unique key at a time.
         *
         * If event has not been published the FlowBus looks for persisted event and
         * returns the persisted one. If there is no even persisted event, there will be
         * an attempt to create a default instance.
         * NOTE! that the default instance may be created with different value for the [key]
         * than what you requested. For these properties the default values will be used.
         *
         * @param clazz The class type of event which you need to receive
         * @param key The primary key of an event instance.
         * */
        override suspend fun <K : Any, E : Event.StickyCollection<K>> getSticky(
            clazz: KClass<E>,
            key: K
        ): E {
            return try {
                flowCommunication.getSticky(clazz, key)
            } catch (notFoundException: NoStickyValueFoundException) {
                try {
                    eventValidator.validateOrThrow(clazz, key)
                    val persisted = persistableEventRegistry.readPersisted(clazz, key)
                    if (persisted.isEmpty()) {
                        stickyEventsFactory.createDefault(clazz)
                    } else {
                        persisted.first()
                    }
                } catch (validationException: WrongEventKeyFormatException) {
                    throw validationException
                }
            }
        }

        /**
         * Deletes the sticky event by the event type.
         *
         * @param clazz The class type of what event to delete.
         * */
        override suspend fun <E : Event.Sticky> delete(clazz: KClass<E>) {
            flowCommunication.delete(clazz)
            persistableEventRegistry.deletePersisted(clazz)
        }

        /**
         * Deletes a sticky event by its key.
         * This method is used to remove a sticky event from the event bus(`EventBus`) by providing its key.
         * The event must be of type [Event.StickyCollection] to have access to the [Event.StickyCollection.key].
         *
         * @param clazz the class of the sticky event to delete. It should extend [Event.StickyCollection].
         * @param key the unique identifier of the event to delete.
         * */
        override suspend fun <K : Any, E : Event.StickyCollection<K>> delete(
            clazz: KClass<E>,
            key: K
        ) {
            flowCommunication.delete(clazz, key)
            persistableEventRegistry.deletePersisted(clazz, key)
        }

        /**
         * Retrieves a collection of sticky events of type [E] derived from [Event.StickyCollection] with the provided
         * [clazz]. Does not need a key, because it returns current collection of each event with the given class,
         * that are stored currently in memory.
         *
         * @param clazz The class of the sticky event collection.
         * @return A collection of sticky events of type [E]. It can be empty if no event of the given type
         * has been published.
         * */
        override suspend fun <K : Any, E : Event.Iterable<K>> getCollection(clazz: KClass<E>): Collection<E> {
            // Read both in-memory events and persistable, because if event
            // gets published to memory, flowCommunication returns non-empty list with only single
            // event instead of the whole collection.
            // The .union() extension function returns set of unique items so that getCollection
            // always returns all possible events for the required StickyCollection type.
            val runtimeCollection = flowCommunication.getCollection(clazz)
            val persistedCollection = persistableEventRegistry.readPersistedCollection(clazz)
            return runtimeCollection.union(persistedCollection)
        }
    }

    open class Base(
        stickyEventsFactory: DefaultStickyEventsFactory,
        flowCommunication: CollectionFlowCommunication,
        persistableEventRegistry: PersistableEventRegistry,
        eventValidator: EventValidator
    ) : Abstract(stickyEventsFactory, flowCommunication, persistableEventRegistry, eventValidator)
}