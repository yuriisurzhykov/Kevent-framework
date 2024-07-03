package com.niceforyou.statemachine.example

import com.github.yuriisurzhykov.kevent.events.Event

sealed interface ExampleEvents : Event {
    data object Play : ExampleEvents
    data object Menu : ExampleEvents
    data object Ping : ExampleEvents
    data object Pong : ExampleEvents
}