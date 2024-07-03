package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  This type of event should be sent only when every AO should be disposed
 *  and should stop to receive events.
 * */
@Serializable
@SerialName("AO_DisposeObjects")
data object DisposeObjects : Event