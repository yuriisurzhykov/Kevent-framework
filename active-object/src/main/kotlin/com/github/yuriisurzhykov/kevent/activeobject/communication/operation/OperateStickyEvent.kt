package com.github.yuriisurzhykov.kevent.activeobject.communication.operation

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

/**
 * An interface that which acts as a bridge to work with sticky mutable map. Due to the fact that
 * for sticky events the map to retain events is one-dimensional and for sticky collection events
 * the map is two-dimensional(`Map<KClass, Map<,>` map of maps), there was a necessity to create
 * a separation for FlowCommunication(that works with SharedFlow and stores events in runtime
 * memory) and the exact map implementation. In other words to move out the logic to work with map
 * from the FlowCommunication.
 * Yes, it makes a system a little more complicated but it creates opportunities to extends
 * the system and its behaviour.
 *
 * Nested interfaces provide base CRUD(create, read, update, delete) operations with very flexible
 * type of sticky events.
 * */
interface OperateStickyEvent {

    /**
     * Interface to emit(store in memory) a sticky event with generic type of map storage and the
     * base event class.
     * */
    interface Emit<
            KlassEvent : Event,
            Map : MutableMap<KClass<out KlassEvent>, *>,
            E : Event> {
        suspend fun emit(map: Map, event: E)
    }

    /**
     * Interface to read a sticky event from the generic type of map storage and the base event class.
     * */
    interface Read<
            KlassEvent : Event,
            T : MutableMap<KClass<out KlassEvent>, *>,
            E : Event> {
        suspend fun read(map: T, clazz: KClass<out E>): KlassEvent?
    }

    /**
     * Interface to delete a sticky event from the generic type of map storage and the base event
     * class.
     * */
    interface Delete<
            KlassEvent : Event,
            T : MutableMap<KClass<out KlassEvent>, *>,
            E : Event> {
        suspend fun delete(map: T, clazz: KClass<out E>)
    }

    /**
     * Interface to read a sticky collection event(either StickyCollection, or any type of
     * partition specific event)  event from the generic type of map storage and the base event class.
     * */
    interface ReadStickyCollection<
            KlassEvent : Event,
            T : MutableMap<KClass<out KlassEvent>, *>> {
        suspend fun read(
            map: T,
            clazz: KClass<out KlassEvent>,
            key: Any
        ): KlassEvent?
    }

    /**
     * Interface to read a sticky collection event(either StickyCollection, or any type of
     * partition specific event) from the generic type of map storage and the base event class.
     * */
    interface DeleteStickyCollection<
            KlassEvent : Event,
            T : MutableMap<KClass<out KlassEvent>, *>> {

        suspend fun delete(
            map: T,
            clazz: KClass<out KlassEvent>,
            key: Any
        )
    }
}