package com.github.yuriisurzhykov.kevent.events.api.sticky

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Interface for deleting sticky events from the event system. Works only with sticky events,
 * so that events inherited from [Event.Sticky].
 * */
interface DeleteSticky {

    /**
     * Deletes the sticky event by the event type.
     * @param clazz The class type of what event to delete.
     * */
    suspend fun <E : Event.Sticky> delete(clazz: KClass<E>)
}