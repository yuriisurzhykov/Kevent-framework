package com.github.yuriisurzhykov.kevent.activeobject.manager

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.AoManagerStateMachine
import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 *  Manager class that regulate initialization process for all AO within application.
 *  The process of initialization is the next:
 *  1. AoManager subscribe all AOs from [aoSet] to [flowBus]
 *  2. After [ActiveObject] instance subscribed to [flowBus] it sends an event [SubscriptionCompleteEvent]
 *  3. AoManager listens for all [SubscriptionCompleteEvent]s from all AOs, and when every AO sent
 *  that event AoManager allows to listen events and sends the very first event in the system that
 *  each AO listens to: [InitPhaseTwoDone]
 *  4. [ActiveObject] instance listens this event under the hood and once it receives that event
 *  it calls function [ActiveObject.publishInitialEvents]
 *  5. After [ActiveObject] published initial events, it calls function [ActiveObject.onCreated]
 *  and it means that AO ready for work.
 * */
class AoManager(
    private val aoSet: Set<ActiveObject>,
    private val flowBus: FlowBus,
    // This param created to be able test this AoManager by passing UnconfinedTestDispatcher
    context: CoroutineContext = AoCoroutineContext()
) : ActiveObject(AoManagerEventFilter(), flowBus, context) {

    private val stateMachine = AoManagerStateMachine(aoSet, flowBus)

    fun startInitialization(): Job = coroutineScope.launch {
        flowBus.subscribe(this@AoManager)
        startEventProcessing()
        stateMachine.initialize()
    }

    override suspend fun notifyActiveObjectSubscribed() {
        // Do nothing in AoManager, because its main controller and nobody should listen its
        // lifecycle.
    }

    override suspend fun publishInitialEvents(flowBus: EventManager) {
        // AoManager doesn't have any events to publish initially. So this method is empty
        // for AoManager class.
    }

    override suspend fun onEvent(event: Event, flowBus: FlowBus) {
        stateMachine.processEvent(event)
    }
}
