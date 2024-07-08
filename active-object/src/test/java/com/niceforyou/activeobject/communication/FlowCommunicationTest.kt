package com.niceforyou.activeobject.communication

import com.github.yuriisurzhykov.kevent.eventbus.communication.FlowCommunication
import com.niceforyou.activeobject.TestStickyEvent
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class FlowCommunicationTest {

    @Test
    fun `test flow communication emits non-sticky event`() = runTest {
        // Given
        val flow = TestSharedFlow()
        val stickyMap = mutableMapOf<KClass<out Event>, Event>()
        val mutexMap = mutableMapOf<KClass<out Event>, Mutex>()
        val communication = FlowCommunication.Base(stickyMap, mutexMap, flow)

        // When
        val expectedEvent = TestEvent()
        communication.emit(expectedEvent)

        // Then
        assertEquals(expectedEvent, flow.emittedValue)
    }

    @Test
    fun `test flow communication emits sticky event`() = runTest {
        // Given
        val flow = TestSharedFlow()
        val stickyMap = mutableMapOf<KClass<out Event>, Event>()
        val mutexMap = mutableMapOf<KClass<out Event>, Mutex>()
        val communication = FlowCommunication.Base(stickyMap, mutexMap, flow)

        // When
        val expectedEvent = TestStickyEvent()
        communication.emit(expectedEvent)

        // Then
        assertEquals(expectedEvent, flow.emittedValue)
    }

    @Test
    fun `test flow communication stores sticky event in map`() = runTest {
        // Given
        val flow = TestSharedFlow()
        val stickyMap = mutableMapOf<KClass<out Event>, Event>()
        val mutexMap = mutableMapOf<KClass<out Event>, Mutex>()
        val communication = FlowCommunication.Base(stickyMap, mutexMap, flow)

        // When
        val expectedEvent = TestStickyEvent()
        communication.emit(expectedEvent)

        // Then
        assertTrue(stickyMap.containsKey(expectedEvent::class))
        val actual = stickyMap[expectedEvent::class]
        assertEquals(expectedEvent, actual)
    }

    @Test
    fun `test flow mutex working as expected`() = runTest {
        // Given
        val sticky = TestStickyEvent()
        val clazz = sticky::class
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val flowCommunication = FlowCommunication.Base(stickyMap, HashMap(), MutableSharedFlow())

        // When
        launch {
            delay(50) // Ensures this coroutine runs after the one below
            flowCommunication.emit(TestStickyEvent(123))
        }
        launch {
            flowCommunication.emit(TestStickyEvent(402))
            assertEquals(TestStickyEvent(402), stickyMap[clazz])
        }

        // Then
        delay(100) // Wait for both coroutines to run
        assertEquals(TestStickyEvent(123), stickyMap[clazz])
    }

    @Test
    fun `test getSticky retrieves correct sticky event from map`() = runTest {
        // Given
        val sticky = TestStickyEvent()
        val clazz = sticky::class
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val flowCommunication = FlowCommunication.Base(stickyMap, HashMap(), MutableSharedFlow())

        // When
        stickyMap[clazz] = sticky
        val actual = flowCommunication.getSticky(clazz)

        // Then
        assertEquals(sticky, actual)
    }

    @Test(expected = NoStickyValueFoundException::class)
    fun `test getSticky throw exception if value not found`() = runTest {
        // Given
        val sticky = TestStickyEvent()
        val clazz = sticky::class
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val flowCommunication = FlowCommunication.Base(stickyMap, HashMap(), MutableSharedFlow())

        // When
        flowCommunication.getSticky(clazz)

        // Then check exception
    }

    @Test
    fun `test delete removes sticky from map`() = runTest {
        // Given
        val sticky = TestStickyEvent()
        val clazz = sticky::class
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val flowCommunication = FlowCommunication.Base(stickyMap, HashMap(), MutableSharedFlow())

        // When
        stickyMap[clazz] = sticky
        assertTrue(stickyMap.containsKey(clazz))
        flowCommunication.delete(clazz)

        // Then
        assertFalse(stickyMap.containsKey(clazz))
    }

    @Test
    fun `test delete removes only passed event type`() = runTest {
        // Internal test resource
        class InternalStickyEvent : Event.Sticky()

        // Given
        val sticky = TestStickyEvent()
        val clazz = sticky::class
        val stickyMap = HashMap<KClass<out Event>, Event>()
        val flowCommunication = FlowCommunication.Base(stickyMap, HashMap(), MutableSharedFlow())

        // When
        stickyMap[clazz] = sticky
        assertTrue(stickyMap.containsKey(clazz))
        flowCommunication.delete(InternalStickyEvent::class)

        // Then
        assertTrue(stickyMap.containsKey(clazz))
    }
}