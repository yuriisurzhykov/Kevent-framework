package com.niceforyou.statemachine

import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StateMachineContextTest {
    @Test
    fun `test context impl provides correct instances`(): Unit = runBlocking {
        val testSl = TestServiceLocator()
        val eventPublisher = mockk<EventManager>(relaxed = true)

        val stateMachine: StateMachine =
            object : StateMachine.Abstract(TestState(), testSl, eventPublisher) {}

        val contextImpl = StateMachineContext.ContextImpl(stateMachine, eventPublisher, testSl)

        assertEquals(stateMachine, contextImpl.currentStateMachine())
        assertEquals(testSl, contextImpl.serviceLocator())
    }

    @Test
    fun `test state machine provides ContextImpl`(): Unit = runBlocking {
        val testSl = TestServiceLocator()
        val eventPublisher = mockk<EventManager>(relaxed = true)

        val stateMachine: StateMachine =
            object : StateMachine.Abstract(TestState(), testSl, eventPublisher) {}

        val contextImpl = StateMachineContext.ContextImpl(stateMachine, eventPublisher, testSl)

        assertEquals(contextImpl, stateMachine.context)
    }
}

private class TestServiceLocator : ServiceLocator