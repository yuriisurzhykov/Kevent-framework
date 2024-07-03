package com.niceforyou.statemachine.example

import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.statemachine.StateMachine
import com.github.yuriisurzhykov.kevent.statemachine.states.State
import io.mockk.mockk

/**
 * [State machine diagram](diagram.png)
 * */
class ExampleStateMachine(
    initialState: State = VirtualApp,
    eventManager: EventManager = mockk<EventManager>(relaxed = true)
) : StateMachine.Abstract(
    initialState,
    ExampleServiceLocator.Base(),
    eventManager
)