package com.github.yuriisurzhykov.kevent.statemachine.states

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.extentions.transitionTo
import com.github.yuriisurzhykov.kevent.statemachine.extentions.unhandled
import com.github.yuriisurzhykov.kevent.statemachine.states.State.Normal
import com.github.yuriisurzhykov.kevent.statemachine.states.State.Transient
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 *  A `State` represents a distinct condition or configuration within a state machine, reflecting
 *  a specific circumstance that an entity, governed by the state machine, might be in.
 *
 *  In a state machine, a `State` encapsulates specific behaviors and transitions. Each `State`
 *  can define actions that occur when entering or exiting the state, as well as behaviors and
 *  transitions to other states based on events. States can be connected through transitions,
 *  forming a graph that represents the entire lifecycle of an entity.
 *
 *  In a Hierarchical State Machine (HSM), states can have a parent-child relationship, forming a
 *  hierarchy of states. A `State` in HSM can be a composite state containing sub-state, or
 *  a simple state without sub-state. This hierarchy allows for more organized and modular design,
 *  where common behaviors can be grouped and managed in parent states, while specific behaviors
 *  are handled in child states.
 *
 *  The `State` interface in this package provides a blueprint for implementing states in a state
 *  machine, defining lifecycle methods such as [onEnter] and [onExit], and a method for processing
 *  events to handle transitions [processEvent].
 *
 *  Nested within are also special types of states like [Normal] for providing a basic implementation,
 *  and [Transient] which triggers transition to a new state right after [onEnter].
 *
 *  __Usage__:
 *  Implementing classes may define the behavior on entering and exiting the state(if they inherits
 *  from State interface), and how events are processed to determine transitions to other
 *  states.
 */
interface State {


    /**
     *  [onEnter] is called when this state becomes the current state. This may happen at startup
     *  if this state is the initial state of the [StateMachine] or when another state processes
     *  an [Event] which results in a transition to this state.
     *
     *  @param context The context of current state machine to which state is being applied.
     * */
    suspend fun onEnter(context: StateMachineContext, params: TransitionParams?)


    /**
     *  This function is called once the current state is going to be replaced
     *  with another [State]. Firstly [StateMachine] calls [onExit] for current
     *  state and after [onEnter] for new state.
     *
     *  @param context The context of current [StateMachine].
     * */
    suspend fun onExit(context: StateMachineContext)

    /**
     *  Every state that inherits from [State] must override this function and return [unhandled],
     *  [handled] or [transitionTo].
     *  @param event This is an event that [StateMachine] received for processing.
     *  @param context [StateMachineContext] that gives you access to state machine where the event
     *  is processing now.
     *  @return [ProcessResult] instance by one of the following functions:
     *  `unhandled(event)`, `handled() or `transitionTo(State)`.
     * */
    suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult


    /**
     *  Provides a typed [ServiceLocator] instance from the [context].
     *
     *  @param context The context of the current state machine.
     *  @return A typed [ServiceLocator] instance.
     *  @throws ClassCastException if the service locator is not of type [T].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ServiceLocator> serviceLocator(context: StateMachineContext): T {
        return context.serviceLocator() as T
    }

    /**
     *  Default implementation of [State] provides empty [onEnter], [onExit] and [initialTransitionState]
     *  functions. For hierarchy it provides ability to set parent to the state in two ways:
     *  directly by providing instance of parent or by lazy function.
     *  @property parent The parent state in hierarchy that has additional logic for handling events
     * */
    abstract class Normal(parent: State?) : State, InitialTransitionState {

        private val parentRef: State? = parent

        /**
         *  Abstract implementation for [onEnter] is the same as for [onExit]. It just provides empty
         *  implementation to meet compiler requirements
         * */
        override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        }

        /**
         *  The [State] interface requires all states to that inherit from it to provide an
         *  implementation for the abstract [onExit]. Many states that inherit from [Normal]
         *  don’t need to do anything on exit, so [Normal] provides this default implementation
         *  that does nothing so that states that inherit from it won’t have to worry about it.
         * */
        override suspend fun onExit(context: StateMachineContext) {}

        /**
         *  By analogy with [onEnter] and [onExit], many states don't have to have transition
         *  after [onEnter] call. So, [Normal] provides empty implementation and returns [null],
         *  that means state doesn't have initial transition.
         * */
        @Suppress("KDocUnresolvedReference")
        override suspend fun initialTransitionState(context: StateMachineContext): State? {
            return null
        }

        /**
         *  Provides reference to parent. If [parent] property is null, then it will create [EmptyState]
         *  which is (`dummy`) object that provides empty implementation for [processEvent]
         * */
        internal fun parent(): State {
            val parent = parentRef
            return parent ?: EmptyState
        }

        internal fun hasParent() = parentRef != null && parentRef != EmptyState
    }

    /**
     *  `Transient` is an abstract class representing a transient state in a state machine. It
     *  extends the [State] interface, providing specific implementations and behaviors suitable
     *  for states that are temporary or intermediate within the state machine's lifecycle.
     *  This class finalizes key functions from the [State] interface, such as [processEvent]
     *  and [onEnter], to standardize behavior and prevent unnecessary overrides in derived classes.
     *  It also introduces an abstract function, [initialTransitionState], which must be implemented
     *  by subclasses to define the transition from this transient state to another state during
     *  [onEnter].
     */
    abstract class Transient : State {

        /**
         *  Overrides the [State.processEvent] function with ‘final’ in order to prevent unnecessary
         *  overriding in derived classes.
         * */
        final override suspend fun processEvent(
            event: Event,
            context: StateMachineContext
        ): ProcessResult = ProcessResult.Handled

        /**
         *  Defines the transition during initialization to be made from this state, if any.
         *
         *  @param context The context of the current state machine.
         *  @return The [State] to transition to.
         */
        abstract suspend fun initialTransitionState(context: StateMachineContext): State

        /**
         *  Overrides the [State.onEnter] function with ‘final’ in order to prevent overriding by
         *  derived classes.
         * */
        final override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
            (context.currentStateMachine() as? StateMachine.Abstract)?.nextState(
                initialTransitionState(context),
                params
            )
        }

        /**
         *  Default implementation of [State.onExit] that does nothing. May be overridden by
         *  derived states.
         * */
        override suspend fun onExit(context: StateMachineContext) {
        }
    }
}