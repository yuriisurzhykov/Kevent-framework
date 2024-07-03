package com.github.yuriisurzhykov.kevent.statemachine.strategy

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.exceptions.RecursiveHierarchyException
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult.Handled
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult.TransitionTo
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult.Unhandled
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 *  ProcessResult is a strategy pattern for processing events. The [State.processEvent] function
 *  must return one of these 3 ProcessResult strategies:
 *  - [TransitionTo] moves [StateMachine] to the state provided in [TransitionTo.state].
 *  - [Unhandled] Causes [StateMachine] to talk to [State] parent if it exists, and calls [State.processEvent]
 *  on parent state
 *  - [Handled] does nothing, indicates only that event is processed and everything is ok.
 * */
sealed interface ProcessResult {

    suspend fun execute(context: StateMachineContext)

    /**
     * `TransitionTo` class implements the strategy pattern for processing events in different
     * states of the [StateMachine]. This one is used to move the [StateMachine] to the state
     * with optional [TransitionParams].
     * */
    data class TransitionTo(
        private val state: State,
        private val params: TransitionParams?
    ) : ProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            (context.currentStateMachine() as? StateMachine.Abstract)?.nextState(state, params)
        }
    }

    /**
     * `Unhandled` class implements the strategy pattern for processing events in different states
     * of the [StateMachine]. This one is used to talk to [State] parent if it exists, and calls
     * [State.processEvent] on parent state.
     * */
    data class Unhandled(
        private val event: Event,
        private val currentState: State.Normal
    ) : ProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            if (currentState.hasParent()) {
                val parent = currentState.parent()
                if (parent is State.Normal && parent.hasParent() && parent.parent() == currentState) {
                    throw RecursiveHierarchyException(currentState, parent)
                }
                currentState
                    .parent()
                    .processEvent(event, context)
                    .execute(context)
            }
        }
    }

    data object Handled : ProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            // Do nothing because we ignored an event
        }
    }
}