package com.github.yuriisurzhykov.kevent.activeobject.manager.hsm

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 * When this state is entering [AoManagerStateMachine] it sends [InitializationCompleteEvent]
 * event to say that [aoSet] finished initialization and ready to process events.
 * */
internal data class InitializationCompleteState(
    private val aoSet: Set<ActiveObject>
) : State.Normal(null) {

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        aoSet.map { ao -> ao.startEventProcessing() }
        context.eventManager().publish(InitializationCompleteEvent)
    }

    override suspend fun processEvent(event: Event, context: StateMachineContext) = handled()
}