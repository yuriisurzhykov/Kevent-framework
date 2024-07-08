package com.niceforyou.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.events.Event
import com.niceforyou.activeobject.TestEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class SubscribeEventFilterTest {

    @Test
    fun `check allow to process if ordinal event in common set`() = runTest {
        val filter = object : EventSubscriberFilter.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(TestEvent::class)
        }

        assertTrue(filter.allowToProcess(TestEvent))
    }

    @Test
    fun `check NOT allow to process if ordinal event in common set`() = runTest {
        val filter = object : EventSubscriberFilter.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(TestEvent::class)
        }

        assertFalse(filter.allowToProcess(FakeOrdinalEvent(1)))
    }

    private data class FakeOrdinalEvent(
        val eventId: Int
    ) : Event
}