@file:OptIn(ExperimentalCoroutinesApi::class)

package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManager
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManagerEventFilter
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.AoManagerStateMachine
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.InitPhaseOneState
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AoManagerTest {

    @Test
    fun `test events for AO manager to subscribe for`() = runTest {
        val aoManagerFilter = AoManagerEventFilter()

        val actual = aoManagerFilter.commonEventsToSubscribe
        val expected = setOf(
            SubscriptionCompleteEvent::class,
            InitPhaseTwoDone::class
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test AoManager subscribes all AOs`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val testAo = FakeActiveObject(EventBus)

        val aoManager = AoManager(setOf(testAo), EventBus, UnconfinedTestDispatcher())

        aoManager.startInitialization().join()
        //delay to be sure async tasks completed execution
        delay(100)

        // Check that subscribed AOs are aoManager itself and testAo
        val expectedSubscribers = setOf(aoManager, testAo)
        val actualSubscribers = EventBus.subscribedAos
        assertEquals(expectedSubscribers, actualSubscribers)
    }

    @Test
    fun `test AoInitializingState subscribe active objects`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitPhaseOneState(setOf(fakeAo), EventBus)

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        state.onEnter(stateMachine.context, TransitionParams.Empty)

        delay(100)

        val expected = setOf(fakeAo)
        val actual = EventBus.subscribedAos

        assertEquals(expected, actual)
    }

    @Test
    fun `test InitPhaseOneState moves to InitPhaseTwoState when all AO ready`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)

        val aoManager = AoManager(setOf(fakeAo), EventBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        val expected = listOf(
            SubscriptionCompleteEvent(ClassSerialWrapper(fakeAo::class)),
            InitPhaseTwoDone(ClassSerialWrapper(fakeAo::class)),
            InitializationCompleteEvent
        )
        val actual = EventBus.sentEvents
        assertEquals(expected, actual)

        assertEquals(1, fakeAo.onCreateCalls)
        assertEquals(1, fakeAo.publishEventsCallCount)
    }

    @Test
    fun `test AO starts receiving events after it is initialized`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)

        val aoManager = AoManager(setOf(fakeAo), EventBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        assertEquals(1, fakeAo.onCreateCalls)
        assertEquals(1, fakeAo.publishEventsCallCount)

        val event = TestEvent()
        EventBus.publish(event)

        val expectedProcessed = listOf(event)
        val actualProcessed = fakeAo.processedEvents

        assertEquals(expectedProcessed, actualProcessed)
    }

    @Test
    fun `test AoManager manages initialization of 100 AO`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val listSize = 100
        val fakeAos = List(listSize) {
            FakeActiveObject(EventBus, context = ImmediateTestDispatcher())
        }

        val aoManager = AoManager(fakeAos.toSet(), EventBus, ImmediateTestDispatcher())
        aoManager.startInitialization()

        val actualCreateCalls = fakeAos.map { it.onCreateCalls }
        val actualPublishCalls = fakeAos.map { it.publishEventsCallCount }
        val expected = List(listSize) { 1 }
        assertEquals(expected, actualCreateCalls)
        assertEquals(expected, actualPublishCalls)
    }
}