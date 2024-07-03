@file:Suppress("UnusedReceiverParameter", "unused")

package com.github.yuriisurzhykov.kevent.statemachine.extentions

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 *  Transitions [StateMachine] to the state provided as [state] parameter. This method provides
 *  nullable extra parameters for transition so the next state will receive a null for parameter.
 *  For future we can easily create a new method with additional [TransitionParams] to pass.
 * */
fun State.Normal.transitionTo(state: State) =
    ProcessResult.TransitionTo(state, null)

/**
 *  If [Event] is processed and there is nothing to do, this function should be called.
 * */
fun State.Normal.handled() = ProcessResult.Handled

/**
 *  If [State] doesn't know how to process an [Event], but it has parent, this function
 *  should be called
 * */
fun State.Normal.unhandled(event: Event) = ProcessResult.Unhandled(event, this)

/**
 * Factory method to simplify construction of simple state machines without a need of creating
 * a lot of addition files and classes.
 * @param state It's an initial state which also possibly can be master(single) state.
 * @param flowBus Instance of [EventManager]. For application only one instance is `EventBus`
 * @param serviceLocator It is an instance of [ServiceLocator] interface that provides some
 * dependencies that can be used in [state].
 * */
fun StateMachine(
    state: State,
    flowBus: EventManager,
    serviceLocator: ServiceLocator = ServiceLocator.Empty()
) = StateMachine.Base(state, serviceLocator, flowBus)