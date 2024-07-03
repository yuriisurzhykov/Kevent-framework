package com.github.yuriisurzhykov.kevent.events.persisted.core.events

/**
 * `PersistableEventRegistry`
 * This interface represents a registry for persistable events.
 * It provides functionality to:
 * - persist events
 * - retrieve persisted sticky events, and retrieve persisted sticky collections, retrieve sticky
 * by its key.
 * - delete sticky event, delete sticky collection event by its key, delete whole sticky collection
 *
 * The actual implementation of this class is always auto-generated and located in build/generated
 * folder. Every class/interface, that implement this [PersistableEventRegistry] has to be annotated
 * with the @[EventRegistry] annotation.
 */
interface PersistableEventRegistry : PersistEvent, ReadPersistedEvent, DeletePersistedEvent