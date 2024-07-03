package com.niceforyou.statemachine.example

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.transitionTo
import com.github.yuriisurzhykov.kevent.statemachine.extentions.unhandled
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

// ExampleStates class created for test only purposes to test
// onEnter and onExit calls amount, to check if state machine
// works properly.
abstract class ExampleStates(parent: State?) : State.Normal(parent) {

    var exitCallCount: Int = 0
    var enterCallCount: Int = 0

    override suspend fun onExit(context: StateMachineContext) {
        super.onExit(context)
        exitCallCount++
    }

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        enterCallCount++
        return super.onEnter(context, params)
    }
}

// Create VirtualApp state that is parent for all other states.
// For Ping Pong diagram this state is transient only, because it
// transition to Menu on entering the state. But we inherit it from
// Example states to use exitCallCount and enterCallCount for tests.

// For the test purpose also we define classes not as object class,
// but as regular class.
internal object VirtualApp : ExampleStates(null) {

    // Transition to Menu when entering
    override suspend fun initialTransitionState(context: StateMachineContext) = Menu

    // Because of we inherits not from State.Transient but from ExampleState,
    // that is State.Normal, we have to override this processEvent and return
    // unhandled to compile tests.
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        unhandled(event)
}

// Creates Menu state with VirtualApp as its parent
internal object Menu : ExampleStates(VirtualApp) {

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Play) transitionTo(VirtualPlay)   // Handle Play event and go to Play state
        else unhandled(event)
}


// Create Play state with VirtualApp as its parent
internal object VirtualPlay : ExampleStates(VirtualApp) {

    // Transitioning to Ping when entering Play
    override suspend fun initialTransitionState(context: StateMachineContext) = Ping

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Menu) transitionTo(Menu)   //Handle Menu event and goe to Menu state
        else unhandled(event)

}


// Create Ping state with Play as its parent
internal object Ping : ExampleStates(VirtualPlay) {
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Pong) transitionTo(Pong)   //Handle Pong event and go to Pong state
        else unhandled(event)
}


// Create Pong state with Play as its parent
internal object Pong : ExampleStates(VirtualPlay) {
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Ping) transitionTo(Ping)   //Handle Ping event and go to Ping state
        else unhandled(event)
}