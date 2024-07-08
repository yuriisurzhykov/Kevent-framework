package com.niceforyou.activeobject.communication

import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class TestSharedFlow : MutableSharedFlow<Event> {
    private val replayCacheList = mutableListOf<Event>()
    private val subscribersState = MutableStateFlow(0)

    lateinit var emittedValue: Event

    override val subscriptionCount: StateFlow<Int>
        get() = subscribersState

    override suspend fun emit(value: Event) {
        emittedValue = value
        replayCacheList.add(value)
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
    }

    override fun tryEmit(value: Event): Boolean {
        emittedValue = value
        replayCacheList.add(value)
        return true
    }

    override val replayCache: List<Event>
        get() = replayCacheList

    override suspend fun collect(collector: FlowCollector<Event>): Nothing {
        try {
            while (true) {
                collector.emit(emittedValue)
            }
        } finally {

        }
    }
}