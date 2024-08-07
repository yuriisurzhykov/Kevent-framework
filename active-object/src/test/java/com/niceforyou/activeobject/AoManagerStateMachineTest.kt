package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.AoManagerStateMachine
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.InitPhaseOneState
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.InitPhaseTwoState
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.InitializationCompleteState
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AoManagerStateMachineTest {

    @Test
    fun `test InitPhaseOneState subscribe AO in onEnter`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitPhaseOneState(setOf(fakeAo), EventBus)

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        state.onEnter(stateMachine.context, null)
        val expected = setOf(fakeAo)
        val actual = EventBus.subscribedAos
        assertEquals(expected, actual)
    }

    @Test
    fun `test InitPhaseOneState moves to InitPhaseTwoState`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitPhaseOneState(setOf(fakeAo), EventBus)

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        val actual = state.processEvent(
            SubscriptionCompleteEvent(ClassSerialWrapper(fakeAo::class)),
            stateMachine.context
        )
        val expected = ProcessResult.TransitionTo(
            InitPhaseTwoState(setOf(fakeAo)),
            null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test InitPhaseTwoState call publish initial events in onEnter`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitPhaseTwoState(setOf(fakeAo))

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        state.onEnter(stateMachine.context, null)
        val expected = 1
        val actual = fakeAo.publishEventsCallCount
        assertEquals(expected, actual)
    }


    @Test
    fun `test InitPhaseOneState moves to InitializationCompleteState`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitPhaseTwoState(setOf(fakeAo))

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        val actual = state.processEvent(
            InitPhaseTwoDone(ClassSerialWrapper(fakeAo::class)),
            stateMachine.context
        )
        val expected = ProcessResult.TransitionTo(
            InitializationCompleteState(setOf(fakeAo)),
            null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test InitializationComplete publishes InitializationCompleteEvent`() = runTest {
        val communication = FakeCommunication()
        val EventBus = FakeEventBus(communication)
        val fakeAo = FakeActiveObject(EventBus)
        val state = InitializationCompleteState(setOf(fakeAo))

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), EventBus)

        state.onEnter(stateMachine.context, null)

        val expected = listOf(InitializationCompleteEvent)
        val actual = EventBus.sentEvents
        assertEquals(expected, actual)
    }
}
