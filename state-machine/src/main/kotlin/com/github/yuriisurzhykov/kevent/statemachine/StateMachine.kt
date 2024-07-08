package com.github.yuriisurzhykov.kevent.statemachine

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.context.ContextProvider
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.exceptions.StateMachineInitializedException
import com.github.yuriisurzhykov.kevent.statemachine.states.InitialTransitionState
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Interface for representing a Hierarchical State Machine (HSM) and
 * Extends [OperateStateMachine] for state operation and [ContextProvider] for context provisioning.
 */
@Suppress("KDocUnresolvedReference")
interface StateMachine : ContextProvider {

    /**
     * Processes the given [event] by calling [State.processEvent] for current state. In terms
     * of HSM that tied to specific AO([ActiveObject]), the event is the one that AO received.
     *
     * @param event The event to process.
     */
    suspend fun processEvent(event: Event)

    /**
     *  Initializes the state machine, setting the initial state and performing any necessary setup.
     * */
    suspend fun initialize()

    fun current(): StateFlow<State>

    /**
     * Abstract class providing a skeletal implementation of [StateMachine]. This uses [StateFlow]
     * for current machine state representation. For [StateMachineContext] it creates
     * [StateMachineContext.ContextImpl] with given [serviceLocator].
     *
     * @property initialState The initial state of the state machine.
     * @property serviceLocator A service locator for dependency injection.
     */
    abstract class Abstract(
        private val initialState: State,
        private val serviceLocator: ServiceLocator,
        private val eventManager: EventManager
    ) : StateMachine {

        private var hasInitialized: Boolean = false
        private val state = MutableStateFlow(initialState)
        private val currentSMContext: StateMachineContext by lazy {
            StateMachineContext.ContextImpl(this, eventManager, serviceLocator)
        }

        override val context: StateMachineContext
            get() = currentSMContext

        /**
         * Initializes the state machine if it hasn't been initialized already.
         * On initialization, invokes [State.onEnter] on the [initialState]. It has __final__
         * modifier so no one will change the logic for state machine initialization.
         *
         * @throws IllegalStateException If the state machine is already initialized.
         */
        final override suspend fun initialize() {
            if (!hasInitialized) {
                hasInitialized = true
                val currentState = state.value
                doStateEnter(currentState, null)
            } else throw StateMachineInitializedException(this::class.simpleName.orEmpty())
        }

        override suspend fun processEvent(event: Event) {
            val eventProcessResult = state.value.processEvent(event, context)
            eventProcessResult.execute(context)
        }

        internal suspend fun nextState(nextState: State, params: TransitionParams?) {
            if (nextState != state.value) {
                state.value.onExit(context)
                state.emit(nextState)
                doStateEnter(state.value, params)
            }
        }

        override fun current(): StateFlow<State> = state.asStateFlow()

        private suspend fun doStateEnter(state: State, params: TransitionParams?) {
            state.onEnter(context, params)
            if (state is InitialTransitionState) {
                val stateToGo = state.initialTransitionState(context)
                if (stateToGo != null) nextState(stateToGo, params)
            }
        }
    }

    class Base(initialState: State, serviceLocator: ServiceLocator, eventBus: EventManager) :
        Abstract(initialState, serviceLocator, eventBus)
}