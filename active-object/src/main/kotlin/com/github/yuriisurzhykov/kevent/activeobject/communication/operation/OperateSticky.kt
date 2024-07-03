package com.github.yuriisurzhykov.kevent.activeobject.communication.operation

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Base implementation for the [OperateStickyEvent] interface with all possible CRUD methods
 * for just a sticky events.
 * The implementation works with one-dimensional map only!
 * */
interface OperateSticky :
    OperateStickyEvent.Emit<Event, MutableMap<KClass<out Event>, Event>, Event.Sticky>,
    OperateStickyEvent.Read<Event, MutableMap<KClass<out Event>, Event>, Event.Sticky>,
    OperateStickyEvent.Delete<Event, MutableMap<KClass<out Event>, Event>, Event.Sticky> {

    open class Base : OperateSticky {
        override suspend fun emit(
            map: MutableMap<KClass<out Event>, Event>,
            event: Event.Sticky
        ) {
            map[event::class] = event
        }

        override suspend fun read(
            map: MutableMap<KClass<out Event>, Event>,
            clazz: KClass<out Event.Sticky>
        ): Event? {
            return map[clazz]
        }

        override suspend fun delete(
            map: MutableMap<KClass<out Event>, Event>,
            clazz: KClass<out Event.Sticky>
        ) {
            map.remove(clazz)
        }
    }
}