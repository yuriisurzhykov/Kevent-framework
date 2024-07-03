package com.github.yuriisurzhykov.kevent.activeobject.manager.hsm

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.extentions.transitionTo
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 *  Once this state enters [AoManagerStateMachine] it forces all AOs in [aoToInitialize] set
 *  to publish initial events either sticky or non-sticky
 * */
internal data class InitPhaseTwoState(
    private val aoToInitialize: Set<ActiveObject>
) : State.Normal(null) {

    // Wrapping up all AOs with ClassSerialWrapper. This list will be used
    // when event PhaseTwoCompleteEvent occur.
    private val notReadyAos = aoToInitialize.map {
        ClassSerialWrapper(it::class)
    }.toMutableList()

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        aoToInitialize.forEach { it.doInternalInitialization() }
    }

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult {
        return when (event) {
            is InitPhaseTwoDone -> {
                notReadyAos.remove(event.instance)
                if (notReadyAos.isEmpty()) {
                    transitionTo(InitializationCompleteState(aoToInitialize))
                } else handled()
            }

            else -> handled()
        }
    }
}