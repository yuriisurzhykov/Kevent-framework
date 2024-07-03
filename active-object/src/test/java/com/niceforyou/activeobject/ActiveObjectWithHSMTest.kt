@file:OptIn(ExperimentalCoroutinesApi::class)

package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObjectWithHSM
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManager
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HsmActiveObjectTest {

    @Test
    fun `test state machine with hsm to check receive events by hsm`() = runTest {
        val stateMachine = TestStateMachine()

        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val hsmAo = TestHsmAo(stateMachine, flowBus)
        val aoManager = AoManager(setOf(hsmAo), flowBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        val testEvent = TestEvent()

        flowBus.publish(testEvent)

        assertEquals(State1, stateMachine.current().value)

        val currentState = stateMachine.current().value as State1

        assertEquals(1, currentState.processEventCallsAmount)
        assertEquals(1, stateMachine.processCallsCount)
        assertEquals(testEvent, stateMachine.processedEvents[0])

        flowBus.publish(testEvent)

        assertEquals(2, currentState.processEventCallsAmount)
        assertEquals(2, stateMachine.processCallsCount)
    }

    @Test
    fun `test state machine throws exception`() = runTest {
        val stateMachine = TestStateMachine()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val hsmAo = TestHsmAo(stateMachine, flowBus)
        val aoManager = AoManager(setOf(hsmAo), flowBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        val testEvent = TestEvent()

        assertEquals(State1, stateMachine.current().value)
        val currentState = stateMachine.current().value as State1
        assertEquals(1, currentState.enterCallCount)

        val exception = RuntimeException()
        stateMachine.processEvent = { throw exception }

        flowBus.publish(testEvent)

        assertEquals(1, currentState.enterCallCount)
        assertEquals(0, currentState.processEventCallsAmount)
        assertEquals(listOf(exception), hsmAo.processedErrors)
    }
}

data object State1 : State.Normal(null) {
    var processEventCallsAmount: Int = 0
    var enterCallCount: Int = 0
    var exitCallCount: Int = 0

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        enterCallCount++
    }

    override suspend fun onExit(context: StateMachineContext) {
        exitCallCount++
    }

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult {
        processEventCallsAmount++
        return handled()
    }
}

internal class TestStateMachine :
    StateMachine.Abstract(State1, ServiceLocator.Empty(), FakeFlowBus(FakeCommunication())) {

    var processCallsCount: Int = 0
    val processedEvents = mutableListOf<Event>()
    var processEvent: suspend (Event) -> Unit = {
        processCallsCount++
        processedEvents.add(it)
    }

    override suspend fun processEvent(event: Event) {
        processEvent.invoke(event)
        super.processEvent(event)
    }
}

private class TestHsmAo(
    stateMachine: StateMachine,
    flowBus: FlowBus
) : ActiveObjectWithHSM(
    stateMachine,
    flowBus,
    FakeSubscribeFilter(TestEvent::class),
    UnconfinedTestDispatcher()
) {

    var publishCallCount: Int = 0
    var created: Boolean = false
    var destroyed: Boolean = false
    val processedErrors = mutableListOf<Throwable>()

    override suspend fun onCreated() {
        super.onCreated()
        created = true
    }

    override suspend fun onDestroy() {
        destroyed = true
    }

    override suspend fun handleError(error: Throwable) {
        processedErrors.add(error)
    }

    override suspend fun publishInitialEvents(flowBus: EventManager) {
        publishCallCount++
    }
}