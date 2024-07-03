package com.github.yuriisurzhykov.kevent.activeobject.manager

import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent

internal class AoManagerEventFilter : EventSubscriberFilter.Base() {

    override val commonEventsToSubscribe = setOf(
        SubscriptionCompleteEvent::class,
        InitPhaseTwoDone::class
    )
}