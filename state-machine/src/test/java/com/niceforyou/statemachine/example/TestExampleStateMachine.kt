package com.niceforyou.statemachine.example

import com.github.yuriisurzhykov.kevent.statemachine.exceptions.StateMachineInitializedException
import com.niceforyou.statemachine.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TestExampleStateMachine {

    @Test(expected = StateMachineInitializedException::class)
    fun `test multiple initialization calls`() = runTest {
        val stateMachine = ExampleStateMachine()
        stateMachine.initialize()
        stateMachine.initialize()
        stateMachine.initialize()
    }

    @Test
    fun `test initial onEnter calls count`() = runTest {
        // Initialize as 0 because class is object and value shared between tests
        VirtualApp.enterCallCount = 0
        val state = VirtualApp
        val stateMachine = ExampleStateMachine(state)
        stateMachine.initialize()

        val actual = state.enterCallCount
        val expected = 1
        assertEquals(expected, actual)
    }

    @Test
    fun `test initial onEnter calls count for Menu`() = runTest {
        // Initialize as 0 because class is object and value shared between tests
        Menu.enterCallCount = 0
        val stateMachine = ExampleStateMachine()
        stateMachine.initialize()

        val currentState = stateMachine.current().value as Menu
        val actual = currentState.enterCallCount
        val expected = 1
        assertEquals(expected, actual)
    }

    @Test
    fun `test initial onExit calls count for App state`() = runTest {
        // Initialize as 0 because class is object and value shared between tests
        VirtualApp.exitCallCount = 0
        val state = VirtualApp
        val stateMachine = ExampleStateMachine(state)
        stateMachine.initialize()

        val actual = state.exitCallCount
        val expected = 1
        assertEquals(expected, actual)
    }

    @Test
    fun `test transition to menu during initializing`() = runTest {
        val stateMachine = ExampleStateMachine()
        stateMachine.initialize()

        val expected = Menu
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
    }

    @Test
    fun `test from menu to play on Play event`() = runTest {
        val stateMachine = ExampleStateMachine(Menu)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Play,)

        val expected = Ping
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
    }

    @Test
    fun `test from game to menu on Menu event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Menu,)

        val expected = Menu
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
    }

    @Test
    fun `test from Ping state to Pong on Pong event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Pong,)

        val expected = Pong
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
    }

    @Test
    fun `test from Pong state to Ping on Ping event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Pong,)

        val expected = Pong
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
    }
}
