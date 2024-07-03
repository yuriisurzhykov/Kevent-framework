package com.github.yuriisurzhykov.kevent.activeobject.manager.events

import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  Event `PhaseTwoReadyEvent` only sent when every event in application subscribed to
 *  FlowBus and ready to send initial sticky and non-sticky events.
 * */
@Serializable
@SerialName("AO_PhaseTwoDone")
internal data class InitPhaseTwoDone(
    @Serializable(with = ClassWrapperSerializer::class) val instance: ClassSerialWrapper
) : Event