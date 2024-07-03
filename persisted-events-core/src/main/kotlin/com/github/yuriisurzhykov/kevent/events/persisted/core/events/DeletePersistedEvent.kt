package com.github.yuriisurzhykov.kevent.events.persisted.core.events

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * This is the `DeletePersistedEvent` interface, extracted from the `PersistableEventRegistry`.
 *
 * This interface defines methods for deleting persisted events, which are critical operations that may need different
 * implementations across different components of the application. Instead of declaring these methods directly in the
 * `PersistableEventRegistry`, they are defined in this interface to provide better separation of responsibilities and
 * to facilitate their mock ability in testing scenarios.
 *
 * Generics are used to ensure type safety with a focus on `Event.Sticky` and `Event.StickyCollection`, since these
 * types play a key role in how we persist events.
 */
interface DeletePersistedEvent {

    /**
     * Method to deletePersisted an event of type `T : Event.Sticky`
     *
     * @param clazz Refers to the `KClass` of the event to be deleted
     */
    suspend fun <T : Event.Sticky> deletePersisted(clazz: KClass<T>)

    /**
     * Method to delete an event collection associated with a specific key of type `K`
     *
     * @param clazz Refers to the `KClass` of the event collection to be deleted
     * @param key Refers to the key of the collection to be deleted
     */
    suspend fun <K : Any, T : Event.StickyCollection<K>> deletePersisted(clazz: KClass<T>, key: K)

    /**
     * Method to clear persisted collection of events of type `T : Event.StickyCollection`
     *
     * @param clazz Refers to the `KClass` of the event collection to be cleared
     */
    suspend fun <K : Any, T : Event.Iterable<K>> clearPersistedCollection(clazz: KClass<T>)
}