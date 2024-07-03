package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

interface EventSubscriberFilter {

    fun allowToProcess(event: Event): Boolean

    /**
     *  Defines the set of events this active object subscribes to.
     *  Must be implemented by subclasses to provide specific event types.
     *
     *  @return Set of [KClass] instances representing event types.
     */
    val commonEventsToSubscribe: Set<KClass<out Event>>

    abstract class Base : EventSubscriberFilter {
        override fun allowToProcess(event: Event): Boolean {
            return event::class in commonEventsToSubscribe
        }
    }
}