package com.github.yuriisurzhykov.kevent.events.api.stickycollection

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * The `DeleteStickyCollection` interface provides a contract for deleting a sticky event
 * by its key.
 * Works only with the [Event.StickyCollection] type to have access to the [Event.StickyCollection.key].
 */
interface DeleteStickyCollection {

    /**
     * Deletes a sticky event by its key.
     * This method is used to remove a sticky event from the event bus(`EventBus`) by providing its key.
     * The event must be of type [Event.StickyCollection] to have access to the [Event.StickyCollection.key].
     *
     * @param clazz the class of the sticky event to delete. It should extend [Event.StickyCollection].
     * @param key the unique identifier of the event to delete.
     *
     * @throws NoSuchElementException if the sticky event with the specified [key] does not exist.
     */
    suspend fun <K : Any, E : Event.StickyCollection<K>> delete(clazz: KClass<E>, key: K)
}