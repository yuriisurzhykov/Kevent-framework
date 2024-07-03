package com.github.yuriisurzhykov.kevent.events.codegen

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Interface for a factory that produces sticky events in the `EventBus`. This factory is responsible
 * for generating instances of sticky events, ensuring that they are never null and have default
 * values as required.
 */
interface DefaultStickyEventsFactory {

    /**
     * Produces an instance of a sticky event of the specified type.
     *
     * @param T The type of sticky event to be produced, extending from `Event.Sticky`.
     * @param kClass The KClass of the sticky event type.
     * @return An instance of the specified sticky event type.
     */
    fun <T : Event.Sticky> createDefault(kClass: KClass<T>): T

    /**
     *  Produces an instance of a sticky event that derived from [Event.StickyCollection]
     *  with the specified type
     *
     *  @param  T type of sticky event
     *  @param  kClass the class type definition for the sticky event
     *
     *  @return An instance of the specified event class type
     * */
    fun <T : Event.StickyCollection<*>> createDefault(kClass: KClass<T>): T
}