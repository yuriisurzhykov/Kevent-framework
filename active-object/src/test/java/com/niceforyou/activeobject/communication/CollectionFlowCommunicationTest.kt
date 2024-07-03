package com.niceforyou.activeobject.communication

import com.github.yuriisurzhykov.kevent.activeobject.communication.CollectionFlowCommunication
import com.niceforyou.activeobject.FakeEvent
import com.niceforyou.activeobject.TestStickyEvent
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class CollectionFlowCommunicationTest {

    @Test
    fun `test flow communication emits non-sticky event`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            stickyMap,
            HashMap(),
            flow
        )

        // When
        val expectedEvent = FakeEvent()
        communication.emit(expectedEvent)

        // Then
        assertEquals(expectedEvent, flow.emittedValue)
        assertFalse(stickyMap.containsKey(expectedEvent::class))
    }

    @Test
    fun `test flow communication emits sticky event`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            stickyMap,
            HashMap(),
            flow
        )
        val expected = TestStickyEvent(12)
        val clazz = expected::class

        // When
        communication.emit(expected)

        // Then
        assertEquals(expected, stickyMap[clazz])
    }

    @Test
    fun `test flow communication emits sticky collection event`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            stickyMap,
            HashMap(),
            flow
        )
        val expected = FakeStickyCollection(2)
        val clazz = expected::class
        val fakeStickyMap = HashMap<Any, Event>()
        stickyCollectionMap[clazz] = fakeStickyMap

        // When
        communication.emit(expected)

        // Then
        assertEquals(expected, fakeStickyMap[2])
        assertFalse(stickyMap.containsKey(clazz))
    }

    @Test
    fun `test flow communication stores multiple same type events wth different keys`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            stickyMap,
            HashMap(),
            flow
        )
        val clazz = FakeStickyCollection::class
        val fakeStickyMap = HashMap<Any, Event>()
        stickyCollectionMap[clazz] = fakeStickyMap

        // When
        val testEvent1 = FakeStickyCollection(1)
        communication.emit(testEvent1)
        val testEvent2 = FakeStickyCollection(2)
        communication.emit(testEvent2)

        // Then
        assertTrue(stickyMap.isEmpty())
        assertEquals(2, fakeStickyMap.values.size)
        assertEquals(testEvent1, fakeStickyMap[1])
        assertEquals(testEvent2, fakeStickyMap[2])
    }

    @Test
    fun `test flow communication returns collection of sticky-collection events`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            HashMap(),
            HashMap(),
            flow
        )
        val testEvent = FakeStickyCollection(1)
        val clazz = testEvent::class
        stickyCollectionMap[clazz] = HashMap<Any, Event>().apply {
            put(1, testEvent)
        }

        // When
        val actual = communication.getCollection(clazz)

        // Then
        val expected = listOf(testEvent)
        assertEquals(expected, actual)
    }

    @Test
    fun `test flow communication deletes sticky-collection by key only`() = runTest {
        // Given
        val stickyCollectionMap =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val flow = TestSharedFlow()
        val communication = CollectionFlowCommunication.Base(
            stickyCollectionMap,
            HashMap(),
            HashMap(),
            flow
        )
        val testEvent = FakeStickyCollection(1)
        val clazz = testEvent::class
        val fakeStickyMap = HashMap<Any, Event>().apply {
            put(1, testEvent)
            put(2, FakeStickyCollection(2))
        }
        stickyCollectionMap[clazz] = fakeStickyMap

        // When
        communication.delete(clazz, 1)

        // Then check
        // removed item not exists in the map now
        assertFalse(fakeStickyMap.containsKey(1))
        // but another still present
        assertTrue(fakeStickyMap.containsKey(2))
    }

    private data class FakeStickyCollection(
        val instanceId: Int
    ) : Event.StickyCollection<Int>() {
        override val key: Int
            get() = instanceId
    }
}