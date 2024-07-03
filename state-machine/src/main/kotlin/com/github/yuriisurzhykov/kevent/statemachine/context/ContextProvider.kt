package com.github.yuriisurzhykov.kevent.statemachine.context

import com.github.yuriisurzhykov.kevent.statemachine.StateMachine

/**
 *  Context provider is an interface that every [StateMachine] have to implement.
 *  This provides reference to [StateMachineContext] that by default is [StateMachineContext.ContextImpl]
 * */
interface ContextProvider {
    val context: StateMachineContext
}