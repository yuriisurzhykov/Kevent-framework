package com.github.yuriisurzhykov.kevent.activeobject.communication

import com.github.yuriisurzhykov.kevent.activeobject.communication.FlowCommunication.Abstract
import com.github.yuriisurzhykov.kevent.activeobject.communication.FlowCommunication.Sticky
import com.github.yuriisurzhykov.kevent.activeobject.communication.operation.OperateSticky
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import com.github.yuriisurzhykov.kevent.events.api.sticky.StickyApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 *  `FlowCommunication` is a wrapper for Kotlin flow, that can emit values and save sticky events
 *  in memory.
 *  `FlowCommunication` interface has 2 implementations:
 *  - [Abstract] implementation that has only logic for emit values
 *  - [Sticky] implementation can work to sticky events, but cannot store collection specific events.
 *  for events
 * */
interface FlowCommunication : StickyApi {

    suspend fun emit(event: Event)

    fun asSharedFlow(): SharedFlow<Event>

    /**
     * Default implementation of [FlowCommunication] interface that provides logic to publish
     * event over [flow] and returns [flow] [asSharedFlow]
     * @property flow Kotlin SharedFlow instance to use to send events
     * */
    abstract class Abstract(
        /** Kotlin SharedFlow with flow of all events within the application */
        private val flow: MutableSharedFlow<Event>
    ) : FlowCommunication {

        override suspend fun emit(event: Event) = flow.emit(event)

        override fun asSharedFlow(): SharedFlow<Event> = flow.asSharedFlow()
    }

    /**
     * [FlowCommunication] interface implementation to work without knowing of collection type,
     * or collection key property in event class.
     * @property    stickyMap is the mutable map, that maps sticky event classes to their actual
     *              last emitted instances.
     * @property    mutexMap mutable map that assign correspond mutex instance to single event type
     * */
    abstract class Sticky(
        /** Mutable map, that maps sticky event classes to their actual last emitted instances */
        private val stickyMap: MutableMap<KClass<out Event>, Event>,
        /** Mutable map that assign correspond mutex instance to single event type */
        protected val mutexMap: MutableMap<KClass<out Event>, Mutex>,
        flow: MutableSharedFlow<Event>
    ) : Abstract(flow) {

        protected abstract val operateSticky: OperateSticky

        override suspend fun emit(event: Event) {
            if (event is Event.Sticky) {
                emitSticky(event)
            }
            super.emit(event)
        }

        /**
         * Saves a sticky event to sticky map.
         *
         * @param event the sticky event to be emitted
         */
        protected open suspend fun emitSticky(event: Event.Sticky) {
            val eventClass = event::class
            // Get Mutex for event type, if there is no Mutex for event, then create it.
            // Locking mutex to prevent race condition when sending and receiving sticky event.
            mutex(eventClass).withLock {
                operateSticky.emit(stickyMap, event)
            }
        }

        /**
         * Retrieves the sticky event of the given class type [clazz].
         *
         * @param clazz the class of the sticky event to retrieve.
         * @return the sticky event of type [E], if found.
         * @throws NoStickyValueFoundException if no sticky event of the given class type is found.
         */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <E : Event.Sticky> getSticky(clazz: KClass<E>): E {
            val stickyEvent = mutex(clazz).withLock { operateSticky.read(stickyMap, clazz) } as? E
            return stickyEvent ?: throw NoStickyValueFoundException(clazz)
        }

        /**
         * Deletes the sticky event of the specified [clazz].
         *
         * @param clazz the class of the sticky event to delete
         */
        override suspend fun <E : Event.Sticky> delete(clazz: KClass<E>) {
            mutex(clazz).withLock { stickyMap.remove(clazz) }
        }

        /**
         * Returns a mutex for the given event class.
         * If a mutex for the event class exists, it is returned,
         * otherwise a new mutex is created and stored in the mutexMap.
         *
         * @param clazz The class of the event for which to obtain a mutex.
         * @return The mutex for the event class.
         */
        protected fun <E : Event> mutex(clazz: KClass<E>): Mutex {
            return mutexMap.getOrPut(clazz) { Mutex() }
        }
    }

    class Base(
        stickyMap: MutableMap<KClass<out Event>, Event>,
        mutexMap: MutableMap<KClass<out Event>, Mutex>,
        flow: MutableSharedFlow<Event>
    ) : Sticky(stickyMap, mutexMap, flow) {
        override val operateSticky = OperateSticky.Base()
    }
}