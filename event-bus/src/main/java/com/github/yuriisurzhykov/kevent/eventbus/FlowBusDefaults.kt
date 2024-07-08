package com.github.yuriisurzhykov.kevent.eventbus

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 *  Defaults for [EventBus] to provide single-point access to default flow to create.
 * */
object EventBusDefaults {

    fun <T : Any> mutableSharedFlow(
        replay: Int = 1,
        extraBufferCapacity: Int = Int.MAX_VALUE,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
    ): MutableSharedFlow<T> {
        return MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)
    }
}