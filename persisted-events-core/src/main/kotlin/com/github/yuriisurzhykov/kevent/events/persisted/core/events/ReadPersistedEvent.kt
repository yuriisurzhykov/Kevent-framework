package com.github.yuriisurzhykov.kevent.events.persisted.core.events

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * `ReadPersistedEvent` is an interface responsible for retrieving persisted event data.
 *
 * These events encapsulate changes in the system, including user actions and system events,
 * that are stored and can be queried later. This interface supplies methods to access these
 * events in different ways, typically based on their classification within the system.
 *
 * The interface provides the basis for implementing a registry of events, which allows
 * managing event persistence in a decoupled manner. In the context of `PersistableEventRegistry`,
 * this interface enables the registry to maintain a list of `Event.Sticky` and `Event.StickyCollection`
 * objects which are persisted for use later.
 *
 * Persistable events include both standalone `Event.Sticky` objects, and collections of these
 * objects, known as `Event.StickyCollection`. Both of these types can be retrieved based on their
 * class (i.e., type).
 *
 * Collections of events also support further querying by a specific key, enabling more granular
 * control over which events are retrieved.
 *
 * Generally, classes implementing this interface enable event sourcing strategies within the system.
 */
interface ReadPersistedEvent {

    /**
     * Retrieves a list of persistable sticky events of specified type.
     *
     * @param clazz The class of the persistable sticky events to be retrieved.
     * @return A list of persistable sticky events matching the specified type.
     */
    suspend fun <T : Event.Sticky> readPersisted(clazz: KClass<T>): List<T>

    /**
     * Retrieves a list of persistable sticky collections of specified type,
     * associated with the specified key.
     *
     * @param clazz The class of the persistable sticky collections to be retrieved.
     * @param key The key associated with the persistable sticky collection.
     * @return A list of persistable sticky collections matching the specified type and key.
     */
    suspend fun <K : Any, T : Event.StickyCollection<K>> readPersisted(
        clazz: KClass<T>,
        key: K
    ): List<T>

    /**
     * Retrieves a list of all persistable sticky collections of specified type.
     *
     * @param clazz The class of the persistable sticky collections to be retrieved.
     * @return A list of all persistable sticky collections matching the specified type.
     */
    suspend fun <K : Any, T : Event.Iterable<K>> readPersistedCollection(clazz: KClass<T>): List<T>
}