package com.github.yuriisurzhykov.kevent.events.persisted.core.events

import com.github.yuriisurzhykov.kevent.events.Event

/**
 * The `PersistEvent` interface is a contract for `PersistableEventManager` or any class that intends to manage and
 * persist events. Implementing this interface indicates that a class has the ability to persist events which implement
 * the [Event.Persistable] interface.
 *
 * It contains a single function, `persist`, which is a suspend coroutine function. The purpose of this function is to
 * take an event object, which must be a type of `Event.Persistable`, and persist, or store it as per the implementation.
 *
 * Implementing `PersistEvent` interface provides a standardized way of persisting events in your application while also
 * promoting a cleaner architecture by following common programming principles like the Interface Segregation Principle.
 */
interface PersistEvent {

    /**
     * Persists the given event object.
     *
     * @param event The event object to be persisted.
     */
    suspend fun <T : Event.Persistable> persist(event: T)
}