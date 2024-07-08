package com.github.yuriisurzhykov.kevent.activeobject.manager.hsm

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.eventbus.EventBus
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.context.StateMachineContext
import com.github.yuriisurzhykov.kevent.statemachine.extentions.handled
import com.github.yuriisurzhykov.kevent.statemachine.extentions.transitionTo
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import com.github.yuriisurzhykov.kevent.statemachine.strategy.ProcessResult
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams

/**
 * This state does three things:
 * - subscribe all AOs from [aoToInitialize] to [EventBus]
 * - listens for [SubscriptionCompleteEvent] and waits for all AOs to send that type of event
 * - when all AOs sent [SubscriptionCompleteEvent], it transition to [InitializationCompleteState]
 * */
internal data class InitPhaseOneState(
    private val aoToInitialize: Set<ActiveObject>,
    private val eventBus: EventBus
) : State.Normal(null) {

    // Wrapping up all AOs with ClassSerialWrapper. This list will be used
    // when event SubscriptionCompleteEvent occur.
    private val notReadyObjectsList = aoToInitialize.map {
        ClassSerialWrapper(it::class)
    }.toMutableList()

    override suspend fun onEnter(context: StateMachineContext, params: TransitionParams?) {
        // Subscribe all AOs to given EventBus
        aoToInitialize.forEach { ao -> ao.subscribeForEvents(eventBus) }
    }

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult {
        return when (event) {
            is SubscriptionCompleteEvent -> {
                // Removing ready AO instance from not ready list
                notReadyObjectsList.remove(event.instance)

                // If there no left not ready AOs move to second initialization phase
                if (notReadyObjectsList.isEmpty()) {
                    transitionTo(InitPhaseTwoState(aoToInitialize))
                } else handled()
            }

            else -> handled()
        }
    }
}