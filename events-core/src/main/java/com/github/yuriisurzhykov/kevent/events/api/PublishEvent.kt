package com.github.yuriisurzhykov.kevent.events.api

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.EventNotValidException

interface PublishEvent {

    /**
     *  Sends an event with type [T] to the bus.
     *
     *  @param T The type of event that extends from Event.
     *  @param event The event instance to be sent.
     *
     *  @throws EventNotValidException If the event has validation logic and validation failed.
     */
    @Throws(EventNotValidException::class)
    suspend fun <T : Event> publish(event: T)
}