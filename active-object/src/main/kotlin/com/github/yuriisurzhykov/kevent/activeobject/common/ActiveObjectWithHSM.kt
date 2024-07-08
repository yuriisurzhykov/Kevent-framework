package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import com.github.yuriisurzhykov.kevent.eventbus.EventBus
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import kotlin.coroutines.CoroutineContext

/**
 *  [ActiveObject] that has state machine and event processing goes to [StateMachine] instance
 *  instead of processing inside of [processEvent] function.
 * */
abstract class ActiveObjectWithHSM(
    private val stateMachine: StateMachine,
    eventBus: EventBus,
    eventFilter: EventSubscriberFilter,
    context: CoroutineContext = AoCoroutineContext()
) : ActiveObject(eventFilter, eventBus, context) {

    override suspend fun onCreated() {
        stateMachine.initialize()
    }

    final override suspend fun onEvent(event: Event, eventBus: EventBus) {
        stateMachine.processEvent(event)
    }
}
