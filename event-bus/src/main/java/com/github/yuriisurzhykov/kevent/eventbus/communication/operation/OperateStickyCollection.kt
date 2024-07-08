package com.github.yuriisurzhykov.kevent.eventbus.communication.operation

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * Generalized interface to work with any type of sticky collection event(either which it's
 * StickyCollection, or any sort of partition specific event types).
 * Work with two-dimensional map(map of maps) to support multi instance storage for same event type.
 * */
interface OperateStickyCollection<E : Event> :
    OperateStickyEvent.Emit<Event, HashMap<KClass<out Event>, HashMap<Any, Event>>, E>,
    OperateStickyEvent.ReadStickyCollection<Event, HashMap<KClass<out Event>, HashMap<Any, Event>>>,
    OperateStickyEvent.DeleteStickyCollection<Event, HashMap<KClass<out Event>, HashMap<Any, Event>>> {
    abstract class Abstract<E : Event> : OperateStickyCollection<E> {
        @Suppress("UNCHECKED_CAST")
        override suspend fun read(
            map: HashMap<KClass<out Event>, HashMap<Any, Event>>,
            clazz: KClass<out Event>,
            key: Any
        ): E? {
            return map[clazz]?.get(key) as? E
        }

        override suspend fun delete(
            map: HashMap<KClass<out Event>, HashMap<Any, Event>>,
            clazz: KClass<out Event>,
            key: Any
        ) {
            map[clazz]?.remove(key)
        }
    }

    open class Collection : Abstract<Event.StickyCollection<*>>() {
        override suspend fun emit(
            map: HashMap<KClass<out Event>, HashMap<Any, Event>>,
            event: Event.StickyCollection<*>
        ) {
            map.getOrPut(event::class) { HashMap() }[event.key] = event
        }
    }
}