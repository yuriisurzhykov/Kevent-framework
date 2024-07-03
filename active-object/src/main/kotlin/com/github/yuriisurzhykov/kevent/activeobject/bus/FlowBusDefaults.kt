package com.github.yuriisurzhykov.kevent.activeobject.bus

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 *  Defaults for [FlowBus] to provide single-point access to default flow to create.
 * */
object FlowBusDefaults {

    fun <T : Any> mutableSharedFlow(
        replay: Int = 1,
        extraBufferCapacity: Int = Int.MAX_VALUE,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
    ): MutableSharedFlow<T> {
        return MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)
    }
}