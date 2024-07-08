package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import com.github.yuriisurzhykov.kevent.eventbus.EventBus
import kotlin.coroutines.CoroutineContext

/**
 *  This class functions as a sort of proxy class, designed to act as an intermediary
 *  in the active object defining process, thereby clarifying that definition of active
 *  object will not have state machine inside of it.
 * */
@Suppress("unused")
abstract class ActiveObjectWithoutHSM(
    eventBus: EventBus,
    eventFilter: EventSubscriberFilter,
    coroutineContext: CoroutineContext = AoCoroutineContext(),
    name: String? = null
) : ActiveObject(eventFilter, eventBus, coroutineContext, name)