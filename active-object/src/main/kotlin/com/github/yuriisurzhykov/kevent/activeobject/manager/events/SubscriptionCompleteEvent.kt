package com.github.yuriisurzhykov.kevent.activeobject.manager.events

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  For each AO abstract implementation sends this event after [ActiveObject.subscribeTo]
 *  is called and AO subscribed for receiving events.
 * */
@Serializable
@SerialName("AO_SubscriptionComplete")
internal data class SubscriptionCompleteEvent(
    @Serializable(with = ClassWrapperSerializer::class) val instance: ClassSerialWrapper
) : Event