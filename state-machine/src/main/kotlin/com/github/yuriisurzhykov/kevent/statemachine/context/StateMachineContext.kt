package com.github.yuriisurzhykov.kevent.statemachine.context

import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine

/**
 *  [StateMachineContext] provides a reference to the current state machine instance and a reference
 *  to the [ServiceLocator] instance which holds dependencies for the current state machine.
 * */
interface StateMachineContext {

    /**
     *  Provides the reference to current state machine in which state is running.
     *  @return [StateMachine] instance
     * */
    fun currentStateMachine(): StateMachine

    /**
     *  @return [ServiceLocator] instance that [StateMachine] keeps as reference. This will give
     *  you ability to get any of required dependencies declared for your specific [StateMachine]
     * */
    fun <T : ServiceLocator> serviceLocator(): T

    /**
     *  All state machines should have an opportunity to publish some events to the [FlowBus].
     *  [FlowBus] implements [EventManager] interface and in any of states you can get access to
     *  that flow bus through [StateMachineContext.eventManager] by calling [EventManager.publish]
     * */
    @Suppress("KDocUnresolvedReference")
    fun eventManager(): EventManager

    /**
     *  Default implementation for context. [StateMachine.Abstract] provides this implementation by
     *  default.
     * */
    class ContextImpl(
        private val stateMachine: StateMachine,
        private val eventManager: EventManager,
        private val sl: ServiceLocator
    ) : StateMachineContext {
        override fun currentStateMachine(): StateMachine = stateMachine

        @Suppress("UNCHECKED_CAST")
        override fun <T : ServiceLocator> serviceLocator(): T = sl as T

        override fun eventManager(): EventManager = eventManager

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ContextImpl

            return stateMachine == other.stateMachine && sl == other.sl
        }

        override fun hashCode(): Int {
            var result = stateMachine.hashCode()
            result = 31 * result + eventManager.hashCode()
            result = 31 * result + sl.hashCode()
            return result
        }

    }
}