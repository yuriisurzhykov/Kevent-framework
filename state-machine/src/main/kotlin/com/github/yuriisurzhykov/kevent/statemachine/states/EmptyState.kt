package com.github.yuriisurzhykov.kevent.statemachine.states

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult

object EmptyState : State.Normal(null) {
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        handled()
}