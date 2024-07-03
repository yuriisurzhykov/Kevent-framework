package com.github.yuriisurzhykov.kevent.events.api.stickycollection

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import kotlin.reflect.KClass

/**
 * The `ReadStickyCollection` interface provides a contract for retrieving a sticky event by its key.
 * Works with the [Event.StickyCollection] type to have access to the [Event.StickyCollection.key].
 */
interface ReadStickyCollection {

    /**
     * Suspends the execution of the current coroutine and retrieves a sticky event of type [E] with the specified [clazz] and [key].
     * The [E] type must extend [Event.StickyCollection] and have a [key] property of type [K].
     *
     * @param clazz The class of the sticky event to retrieve.
     * @param key The key of the sticky event to retrieve.
     * @return The retrieved sticky event of type [E].
     * @throws NoStickyValueFoundException if no sticky event with the specified [clazz] and [key] is found.
     * */
    suspend fun <K : Any, E : Event.StickyCollection<K>> getSticky(clazz: KClass<E>, key: K): E
}