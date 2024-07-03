package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import kotlin.coroutines.CoroutineContext

/**
 *  [ActiveObject] that has state machine and event processing goes to [StateMachine] instance
 *  instead of processing inside of [processEvent] function.
 * */
abstract class ActiveObjectWithHSM(
    private val stateMachine: StateMachine,
    flowBus: FlowBus,
    eventFilter: EventSubscriberFilter,
    context: CoroutineContext = AoCoroutineContext()
) : ActiveObject(eventFilter, flowBus, context) {

    override suspend fun onCreated() {
        stateMachine.initialize()
    }

    final override suspend fun onEvent(event: Event, flowBus: FlowBus) {
        stateMachine.processEvent(event)
    }

    fun getStateMachineContext() = stateMachine.context
}
