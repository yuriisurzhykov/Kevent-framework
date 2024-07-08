package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

class FakeSubscribeFilter : EventSubscriberFilter.Base {

    private val subscribeClass: KClass<out Event>

    constructor() {
        subscribeClass = TestEvent::class
    }

    constructor(eventClass: KClass<out Event>) {
        subscribeClass = eventClass
    }

    override val commonEventsToSubscribe: Set<KClass<out Event>>
        get() = setOf(subscribeClass)
}