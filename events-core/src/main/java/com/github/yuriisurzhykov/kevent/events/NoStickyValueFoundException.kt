package com.github.yuriisurzhykov.kevent.events

import com.github.yuriisurzhykov.kevent.events.api.EventManager
import kotlin.reflect.KClass

/**
 *  Exception that [EventManager] throws whenever they couldn't find sticky event for
 *  the class.
 * */
class NoStickyValueFoundException(clazz: KClass<out Event>) :
    NoSuchElementException("No sticky value found for class: $clazz")