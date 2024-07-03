package com.niceforyou.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.niceforyou.activeobject.FakeEvent
import com.github.yuriisurzhykov.kevent.events.Event
import com.niceforyou.events.api.partition.core.PartitionId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class SubscribeEventFilterTest {

    @Test
    fun `check allow to process partition event if it defined in common set`() = runTest {
        val filter = object : EventSubscriberFilter.Partition.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(FakeEvent::class, FakePartitionEvent::class)
            override val partition: PartitionId = PartitionId.P1
            override val partitionEventsToSubscribe: Set<KClass<out Event.PartitionSpecific>> =
                emptySet()
        }

        PartitionId.entries.forEach { partition ->
            assertTrue(filter.allowToProcess(FakePartitionEvent(partition)))
        }
    }

    @Test
    fun `check NOT allow to process partition event if it's in partition set`() = runTest {
        val filter = object : EventSubscriberFilter.Partition.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(FakeEvent::class)
            override val partition: PartitionId = PartitionId.P1
            override val partitionEventsToSubscribe: Set<KClass<out Event.PartitionSpecific>> =
                setOf(FakePartitionEvent::class)
        }

        assertTrue(filter.allowToProcess(FakePartitionEvent(PartitionId.P1)))

        (PartitionId.entries - PartitionId.P1).forEach { partition ->
            assertFalse(filter.allowToProcess(FakePartitionEvent(partition)))
        }
    }

    @Test
    fun `check allow to process if ordinal event in common set`() = runTest {
        val filter = object : EventSubscriberFilter.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(FakeEvent::class)
        }

        assertTrue(filter.allowToProcess(FakeEvent()))
    }

    @Test
    fun `check NOT allow to process if ordinal event in common set`() = runTest {
        val filter = object : EventSubscriberFilter.Base() {
            override val commonEventsToSubscribe: Set<KClass<out Event>> =
                setOf(FakeEvent::class)
        }

        assertFalse(filter.allowToProcess(FakeOrdinalEvent(1)))
    }

    private data class FakePartitionEvent(
        override val partition: PartitionId
    ) : Event.PartitionSpecific

    private data class FakeOrdinalEvent(
        val eventId: Int
    ) : Event
}