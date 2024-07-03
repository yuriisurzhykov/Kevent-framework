package com.github.yuriisurzhykov.kevent.activeobject.manager

import com.github.yuriisurzhykov.kevent.activeobject.common.DisposeObjects
import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassWrapperSerializer
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 *  Kotlin Serialization specification for internal event classes to serialize them in log file.
 * */
fun SerializersModuleBuilder.aoManagerPolymorphics() {
    polymorphic(Event::class) {
        subclass(SubscriptionCompleteEvent::class)
        subclass(InitPhaseTwoDone::class)
        subclass(InitializationCompleteEvent::class)
        subclass(DisposeObjects::class)
    }
    contextual(ClassWrapperSerializer)
}