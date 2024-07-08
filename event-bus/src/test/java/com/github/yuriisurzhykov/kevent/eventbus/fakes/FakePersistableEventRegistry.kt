package com.github.yuriisurzhykov.kevent.eventbus.fakes

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEventRegistry
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class FakePersistableEventRegistry : PersistableEventRegistry {
    var calledPersistCount = 0
    var deleteStickyCount = 0
    var deleteStickyCollectionCount = 0
    var readStickyCallCount = 0
    var readStickyCollectionCallCount = 0
    var calledClearPersistedCollectionCount = 0

    var fakePersistedSticky: Event.Sticky? = null
    var fakePersistedStickyCollection: Event.StickyCollection<*>? = null

    override suspend fun <T : Event.Sticky> deletePersisted(clazz: KClass<T>) {
        deleteStickyCount++
        fakePersistedSticky = null
    }

    override suspend fun <K : Any, T : Event.StickyCollection<K>> deletePersisted(
        clazz: KClass<T>,
        key: K
    ) {
        deleteStickyCollectionCount++
        fakePersistedStickyCollection = null
    }

    override suspend fun <K : Any, T : Event.Iterable<K>> clearPersistedCollection(clazz: KClass<T>) {
        calledClearPersistedCollectionCount++
        fakePersistedStickyCollection = null
    }

    override suspend fun <T : Event.Persistable> persist(event: T) {
        calledPersistCount++
    }

    override suspend fun <T : Event.Sticky> readPersisted(clazz: KClass<T>): List<T> {
        readStickyCallCount++
        return fakePersistedSticky?.let { listOf(it as T) } ?: emptyList()
    }

    override suspend fun <K : Any, T : Event.StickyCollection<K>> readPersisted(
        clazz: KClass<T>,
        key: K
    ): List<T> {
        readStickyCollectionCallCount++
        return fakePersistedStickyCollection?.let { listOf(it as T) } ?: emptyList()
    }


    override suspend fun <K : Any, T : Event.Iterable<K>> readPersistedCollection(clazz: KClass<T>): List<T> {
        return (fakePersistedStickyCollection?.let { listOf(it as T) }
            ?: emptyList())
    }
}