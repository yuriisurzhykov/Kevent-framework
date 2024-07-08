package com.github.yuriisurzhykov.kevent.eventbus.communication

import com.github.yuriisurzhykov.kevent.eventbus.communication.operation.OperateSticky
import com.github.yuriisurzhykov.kevent.eventbus.communication.operation.OperateStickyCollection
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import com.github.yuriisurzhykov.kevent.events.api.stickycollection.StickyCollectionApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * The CollectionFlowCommunication interface extends the FlowCommunication and StickyCollectionApi interfaces.
 * It represents Kotlin's flow wrapper for interacting with flow communication that also supports sticky event
 * collections in the event system.
 * It provides methods for emitting events, emitting sticky collections, deleting sticky events from collections,
 * retrieving sticky events from collections, and retrieving collections of sticky events.
 * */
interface CollectionFlowCommunication :
    FlowCommunication,
    StickyCollectionApi {

    abstract class Abstract(
        private val stickyCollectionMap: HashMap<KClass<out Event>, HashMap<Any, Event>>,
        stickyMap: MutableMap<KClass<out Event>, Event>,
        mutexMap: MutableMap<KClass<out Event>, Mutex>,
        flow: MutableSharedFlow<Event>
    ) : FlowCommunication.Sticky(stickyMap, mutexMap, flow), CollectionFlowCommunication {

        protected abstract val operateCollection: OperateStickyCollection.Collection

        protected open suspend fun emitStickyCollection(event: Event) {
            when (event) {
                is Event.StickyCollection<*>          -> {
                    operateCollection.emit(stickyCollectionMap, event)
                }
            }
        }

        /**
         * Emits the given event. If the event is an instance of [Event.StickyCollection], it calls
         * [operateCollection] function to handle the sticky collection event. Finally, it calls
         * the [FlowCommunication.Sticky.emit] function to emit the event.
         *
         * @param event the event to be emitted
         */
        override suspend fun emit(event: Event) {
            mutex(event::class).withLock {
                emitStickyCollection(event)
            }
            super.emit(event)
        }

        /**
         * Deletes a sticky event of the specified class and key.
         *
         * @param clazz the class of the sticky event to delete
         * @param key the key of the sticky event to delete
         */
        override suspend fun <K : Any, E : Event.StickyCollection<K>> delete(
            clazz: KClass<E>,
            key: K
        ): Unit = mutex(clazz).withLock {
            operateCollection.delete(stickyCollectionMap, clazz, key)
        }

        /**
         * Retrieves the sticky event with the specified class and key.
         *
         * @param clazz The class of the sticky event.
         * @param key The unique identifier of the sticky event.
         * @return The sticky event if found, otherwise throws a [NoStickyValueFoundException].
         */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <K : Any, E : Event.StickyCollection<K>> getSticky(
            clazz: KClass<E>,
            key: K
        ): E {
            val stickyEvent = mutex(clazz).withLock {
                operateCollection.read(stickyCollectionMap, clazz, key) as? E
            }
            return stickyEvent ?: throw NoStickyValueFoundException(clazz)
        }

        /**
         * Retrieves a collection of events of type [E] from the stickyCollectionMap.
         *
         * @param clazz the class of the event
         * @return the collection of events of type [E] from the stickyCollectionMap
         */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <K : Any, E : Event.Iterable<K>> getCollection(clazz: KClass<E>): Collection<E> {
            val collectionResult = mutex(clazz).withLock {
                return@withLock stickyCollectionMap[clazz].orEmpty().values.map {
                    it as E
                }
            }
            return collectionResult
        }
    }

    class Base(
        stickyCollectionMap: HashMap<KClass<out Event>, HashMap<Any, Event>>,
        stickyMap: MutableMap<KClass<out Event>, Event>,
        mutexMap: MutableMap<KClass<out Event>, Mutex>,
        flow: MutableSharedFlow<Event>
    ) : Abstract(stickyCollectionMap, stickyMap, mutexMap, flow) {
        override val operateCollection = OperateStickyCollection.Collection()
        override val operateSticky = OperateSticky.Base()
    }
}