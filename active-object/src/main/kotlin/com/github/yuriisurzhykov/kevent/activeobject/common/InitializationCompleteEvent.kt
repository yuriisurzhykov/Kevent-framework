package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  This event occur only when all ActiveObjects have subscribed to [EventBus] and published
 *  initial sticky and non-sticky events. In other words this event occur when initialization
 *  complete.
 * */
@Serializable
@SerialName("AO_InitializationComplete")
data object InitializationCompleteEvent : Event