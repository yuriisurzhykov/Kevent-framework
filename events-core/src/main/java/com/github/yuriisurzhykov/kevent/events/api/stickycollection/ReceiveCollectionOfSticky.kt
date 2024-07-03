package com.github.yuriisurzhykov.kevent.events.api.stickycollection

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Represents an interface for receiving collections of the sticky events derived from [Event.StickyCollection].
 * */
interface ReceiveCollectionOfSticky {

    /**
     * Retrieves a collection of sticky events of type [E] derived from [Event.StickyCollection] with the provided [clazz].
     * Does not need a key, because it returns current collection of each event with the given class, that are
     * stored currently in memory.
     *
     * @param clazz The class of the sticky event collection.
     * @return A collection of sticky events of type [E]. It can be empty if no event of the given type
     * has been published.
     */
    suspend fun <K : Any, E : Event.Iterable<K>> getCollection(
        clazz: KClass<E>
    ): Collection<E>
}