package com.github.yuriisurzhykov.kevent.events

import com.github.yuriisurzhykov.kevent.events.Event.Sticky
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.events.codegen.DefaultableStickyEvent
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import kotlinx.serialization.Polymorphic

/**
 *  This class doesn't have any functions. It works just as an indicator to mark classes as events.
 *  It also annotated with [Polymorphic] annotation to make all further events serializable.
 *  In addition there is [Sticky] interface that is also only indicator interface without functions.
 *  The process of creating event may be the following:
 *  - You have to create sealed interface for your group of events inside _core/events_ application
 *  folder.
 *  - Then you also have to annotate it with [Polymorphic] annotation.
 *  - Create inner classes (data class/data object) for your certain event(-s).
 *  ```
 *  @Polymorphic
 *  sealed interface GroupOfEvents : Event {
 *      @Serializable
 *      data object CertainEvent: GroupOfEvents
 *
 *      @Serializable
 *      data object CertainStickyEvent: GroupOfEvents, Event.Sticky
 *  }
 *  ```
 * */
@Polymorphic
interface Event {

    /**
     * If the event needs complex validation steps which should be executed before the event
     * is being sent. If the validation is not success this method should return the exception
     * instance that will be through instead of emitting the event.
     * */
    @Polymorphic
    interface Validatable {

        /**
         * Any custom validation logic can be done here. [EventNotValidException] should be
         * returned if validation wasn't success.
         * @param eventManager The FlowBus instance to use if there is a need to read extra sticky
         * events.
         * @return [EventNotValidException] instance if the exception should occur or null if
         * validation success or no validation needed.
         * */
        @Throws(EventNotValidException::class)
        suspend fun validate(eventManager: EventManager): EventNotValidException? = null
    }

    interface KeyValidatable<K : Any> : Event {
        val key: K
        fun validationRule(): ValidateEventKey<K>? = null
    }

    interface Iterable<T : Any> : Event

    /**
     *  Events with payloads may optionally inherit from this [Sticky] interface to cause the event
     *  to be stored in runtime memory. Any code can read the payload of a Sticky event at any
     *  time by using the `FlowBus.getSticky` method.
     *  If you inherit class from this interface, you also have to mark you class with [DefaultableStickyEvent]
     *  annotation to have the kotlin compiler generate factory of default values.
     * */
    @Polymorphic
    abstract class Sticky : Event, Validatable

    /**
     *  Abstract class [StickyCollection] represents event class, that should be sticky(latched
     *  in memory) and always should have a key, in order to everyone would have ability
     *  to filter or do whatever they need to do based on the key.
     *  @property key gives a unique identifier to identify an event instance
     * */
    @Polymorphic
    abstract class StickyCollection<K : Any> : KeyValidatable<K>, Validatable, Iterable<K>

    /**
     * Event that is need to be persisted in database automatically should be inherited from
     * this interface. In addition to inheritance that event class should be annotated with
     * the `@PersistableEvent` annotation.
     * Every time when [Persistable] event is being sent it automatically gets persisted in
     * database.
     * */
    interface Persistable
}