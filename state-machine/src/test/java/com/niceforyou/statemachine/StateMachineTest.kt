package com.niceforyou.statemachine

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.niceforyou.statemachine.example.Menu
import com.niceforyou.statemachine.example.Ping
import com.niceforyou.statemachine.example.Pong
import com.niceforyou.statemachine.example.VirtualApp
import com.niceforyou.statemachine.example.VirtualPlay
import com.github.yuriisurzhykov.kevent.statemachine.exceptions.StateMachineInitializedException
import com.github.yuriisurzhykov.kevent.statemachine.extentions.transitionTo
import com.github.yuriisurzhykov.kevent.statemachine.extentions.unhandled
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class StateMachineTest {

    private val testInitialStateName = TestInitialState::class.simpleName
    private val testStateName = TestState::class.simpleName

    @Test
    fun `test onExit called before onEnter for normal state`() = runTest {
        callsOrderList.clear()

        val stateToGoTo = TestState()
        val initialState = TestInitialState(stateToGoTo)
        val eventPublisher = mockk<EventManager>(relaxed = true)
        val stateMachine = TestStateMachine(initialState, eventPublisher)

        stateMachine.initialize()

        var actualCallOrder = callsOrderList
        var expectedOrder =
            listOf(
                "$testInitialStateName\$onEnter",
                "$testInitialStateName\$initialTransitionState"
            )

        assertEquals(expectedOrder, actualCallOrder)

        stateMachine.processEvent(object : Event {})

        expectedOrder = listOf(
            "$testInitialStateName\$onEnter",
            "$testInitialStateName\$initialTransitionState",
            "$testInitialStateName\$onExit",
            "$testStateName\$onEnter",
            "$testStateName\$initialTransitionState",
        )
        actualCallOrder = callsOrderList

        assertEquals(expectedOrder, actualCallOrder)

        assertEquals(1, initialState.onEnterCallsCount)
        assertEquals(1, initialState.onExitCallsCount)
        assertEquals(1, initialState.initialTransitionCallsCount)
        assertEquals(1, stateToGoTo.onEnterCallsCount)
        assertEquals(0, stateToGoTo.onExitCallsCount)
        assertEquals(1, stateToGoTo.initialTransitionCallsCount)
        callsOrderList.clear()
    }

    @Test(expected = StateMachineInitializedException::class)
    fun `test state machine throws exception if it was initialized`() = runTest {
        callsOrderList.clear()
        val eventManager = mockk<EventManager>(relaxed = true)
        val stateMachine = TestStateMachine(TestState(), eventManager)
        stateMachine.initialize()
        stateMachine.initialize()
    }
}

internal val callsOrderList = mutableListOf<String>()

private class TestStateMachine(initialState: State, eventManager: EventManager) :
    StateMachine.Abstract(initialState, ServiceLocator.Empty(), eventManager)

internal abstract class AbstractTestState : State.Normal(null) {

    var processEventCallsCount: Int = 0
    var initialTransitionCallsCount: Int = 0
    var onEnterCallsCount: Int = 0
    var onExitCallsCount: Int = 0

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        onEnterCallsCount++
        callsOrderList.add("${this::class.simpleName}\$onEnter")
    }

    override suspend fun initialTransitionState(context: StateMachineContext): State? {
        initialTransitionCallsCount++
        callsOrderList.add("${this::class.simpleName}\$initialTransitionState")
        return null
    }

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult {
        processEventCallsCount++
        callsOrderList.add("${this::class.simpleName}\$processEvent")
        return unhandled(event)
    }

    override suspend fun onExit(context: StateMachineContext) {
        onExitCallsCount++
        callsOrderList.add("${this::class.simpleName}\$onExit")
    }
}

private class TestInitialState(
    private val stateToGoTo: State
) : AbstractTestState() {
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult {
        return transitionTo(stateToGoTo)
    }
}

internal class TestState : AbstractTestState()

internal inline fun runTest(crossinline block: suspend () -> Unit): Unit =
    kotlinx.coroutines.test.runTest {
        block.invoke()
        VirtualApp.enterCallCount = 0
        VirtualApp.exitCallCount = 0
        VirtualPlay.enterCallCount = 0
        VirtualPlay.exitCallCount = 0
        Ping.enterCallCount = 0
        Ping.exitCallCount = 0
        Pong.enterCallCount = 0
        Pong.exitCallCount = 0
        Menu.enterCallCount = 0
        Menu.exitCallCount = 0
    }