package com.github.yuriisurzhykov.kevent.eventbus.communication

import com.github.yuriisurzhykov.kevent.eventbus.EventBusDefaults
import com.github.yuriisurzhykov.kevent.eventbus.communication.FlowCommunicationBuilder.Common
import com.github.yuriisurzhykov.kevent.eventbus.communication.FlowCommunicationBuilder.StickyCollection
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass

/**
 *  Builder interface to build different type of [FlowCommunication].
 *  There are two implementations of [FlowCommunicationBuilder]:
 *  - [Common] that builds regular [FlowCommunication]
 *  - [StickyCollection] that builds [CollectionFlowCommunication] with sticky collection ability.
 * */
interface FlowCommunicationBuilder<T : FlowCommunication> {

    fun stickyMap(): MutableMap<KClass<out Event>, Event> = mutableMapOf()
    fun mutexMap(): MutableMap<KClass<out Event>, Mutex> = mutableMapOf()
    fun sharedFlow(): MutableSharedFlow<Event> = EventBusDefaults.mutableSharedFlow()

    fun build(): T

    /**
     * This object represents a common class that implements the [FlowCommunicationBuilder] interface.
     * It builds a [FlowCommunication] instance.
     */
    @Suppress("unused")
    object Common : FlowCommunicationBuilder<FlowCommunication> {
        /**
         * Builds a [FlowCommunication] instance.
         *
         * @return The built [FlowCommunication] instance.
         */
        override fun build(): FlowCommunication =
            FlowCommunication.Base(stickyMap(), mutexMap(), sharedFlow())
    }

    /**
     * The StickyCollection class represents a builder for building [CollectionFlowCommunication] instances.
     * It implements the [FlowCommunicationBuilder] interface.
     */
    object StickyCollection : FlowCommunicationBuilder<CollectionFlowCommunication> {

        /**
         * Returns a hashmap that stores sticky collections of events.
         *
         * @return hashmap containing sticky collections of events
         */
        private fun stickyCollectionMap() = HashMap<KClass<out Event>, HashMap<Any, Event>>()

        /**
         * Builds a [CollectionFlowCommunication] instance.
         *
         * @return a [CollectionFlowCommunication] instance with the following parameters:
         * - stickyCollectionMap: a map that maps sticky event classes to their corresponding sticky collections
         * - stickyMap: a map that maps event classes to their corresponding sticky events
         * - mutexMap: a map that maps event classes to their corresponding mutex instances
         * - sharedFlow: a shared flow for emitting events
         */
        override fun build(): CollectionFlowCommunication =
            CollectionFlowCommunication.Base(
                stickyCollectionMap(),
                stickyMap(),
                mutexMap(),
                sharedFlow()
            )
    }
}