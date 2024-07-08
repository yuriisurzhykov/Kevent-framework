package com.github.yuriisurzhykov.kevent.eventbus.fakes

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.codegen.DefaultStickyEventsFactory
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class FakeStickyComponentsFactory : DefaultStickyEventsFactory {

    var produceStickyCallCount = 0
    var produceStickyCollectionCallCount = 0

    lateinit var defaultSticky: Event.Sticky
    lateinit var defaultStickyCollection: Event.StickyCollection<*>

    override fun <T : Event.Sticky> createDefault(kClass: KClass<T>): T {
        produceStickyCallCount++
        return defaultSticky as T
    }

    override fun <T : Event.StickyCollection<*>> createDefault(kClass: KClass<T>): T {
        produceStickyCollectionCallCount++
        return defaultStickyCollection as T
    }
}