package com.github.yuriisurzhykov.kevent.events.api.sticky

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Represents an interface for reading sticky events from the event system.
 */
interface ReadSticky {

    /**
     * Retrieves the sticky event of the specified class from the event system.
     *
     * @param clazz The class of the sticky event to retrieve.
     * @return The instance of a sticky event of the specified class.
     */
    suspend fun <E : Event.Sticky> getSticky(clazz: KClass<E>): E
}