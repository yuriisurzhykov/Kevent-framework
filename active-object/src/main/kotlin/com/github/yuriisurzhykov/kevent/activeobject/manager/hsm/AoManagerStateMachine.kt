package com.github.yuriisurzhykov.kevent.activeobject.manager.hsm

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManager
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.context.ServiceLocator

/**
 *  Internal class for state machine related to [AoManager] instance.
 *  It has 3 states: [InitPhaseOneState], [InitPhaseTwoState] and [InitializationCompleteState]
 *  In [InitPhaseOneState] state it processes [SubscriptionCompleteEvent] and waits for all AOs
 *  to sent that type of event.
 *  in [InitPhaseTwoState] state it processes [InitPhaseTwoDone] event and waits for all AOs
 *  *  to sent that type of event.
 *  in [InitializationCompleteState] when it entering it sends [InitializationCompleteEvent] and
 *  allow AOs to process events.
 * */
internal class AoManagerStateMachine(
    aoSet: Set<ActiveObject>,
    flowBus: FlowBus
) : StateMachine.Abstract(
    InitPhaseOneState(aoSet, flowBus),
    ServiceLocator.Empty(),
    flowBus
)