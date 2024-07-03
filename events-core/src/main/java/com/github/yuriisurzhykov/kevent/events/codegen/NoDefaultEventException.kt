package com.github.yuriisurzhykov.kevent.events.codegen

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * The `NoDefaultEventException` may be thrown when a user (developer) is trying to read
 * a sticky event that wasn't published to the `EventBus` but wasn't annotated with the `@StickyComponent`
 * annotation.
 * */
class NoDefaultEventException(clazz: KClass<out Event>) :
    RuntimeException(
        "Cannot create event of type $clazz. " +
                "The @StickyComponent annotation is required to create a default instance of an event."
    )