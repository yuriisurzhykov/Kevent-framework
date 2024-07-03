package com.niceforyou.activeobject.bus.fakes

import com.github.yuriisurzhykov.kevent.activeobject.communication.CollectionFlowCommunication
import com.github.yuriisurzhykov.kevent.activeobject.communication.FlowCommunicationBuilder
import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class FakeFlowBusCommunication :
    CollectionFlowCommunication.Abstract(
        HashMap(),
        mutableMapOf(),
        mutableMapOf(),
        FlowCommunicationBuilder.StickyCollection.sharedFlow()
    ) {

    val emittedList = mutableListOf<Event>()
    val emittedSticky = mutableMapOf<KClass<out Event>, Event>()
    var emittedStickyCollection = mutableMapOf<KClass<out Event>, Event>()

    var returnSticky: ((KClass<out Event.Sticky>) -> Event.Sticky)? = null
    var returnStickyCollection: ((KClass<out Event.StickyCollection<*>>, Any) -> Event.StickyCollection<*>)? =
        null

    var deleteStickyCount: Int = 0
    var deleteStickyCollectionCount: Int = 0
    var deleteStickyPartitionCount: Int = 0
    var deleteStickyPartitionCollectionCount: Int = 0

    override val operateCollection = FakeOperateStickyCollection()
    override val operateSticky = FakeOperateSticky()

    override suspend fun emit(event: Event) {
        super.emit(event)
        emittedList.add(event)
    }

    override suspend fun emitSticky(event: Event.Sticky) {
        super.emitSticky(event)
        emittedSticky[event::class] = event
    }

    override suspend fun emitStickyCollection(event: Event) {
        return super.emitStickyCollection(event).apply {
            emittedStickyCollection[event::class] = event
        }
    }

    override suspend fun <E : Event.Sticky> delete(clazz: KClass<E>) {
        super.delete(clazz)
        deleteStickyCount++
        emittedSticky.remove(clazz)
    }

    override suspend fun <K : Any, E : Event.StickyCollection<K>> delete(clazz: KClass<E>, key: K) {
        super.delete(clazz, key)
        deleteStickyCollectionCount++
        emittedStickyCollection.remove(clazz)
    }

    override suspend fun <K : Any, E : Event.Iterable<K>> getCollection(clazz: KClass<E>): Collection<E> {
        return emittedStickyCollection.values as Collection<E>
    }

    override suspend fun <E : Event.Sticky> getSticky(clazz: KClass<E>): E {
        return returnSticky?.invoke(clazz) as? E ?: super.getSticky(clazz)
    }

    override suspend fun <K : Any, E : Event.StickyCollection<K>> getSticky(
        clazz: KClass<E>,
        key: K
    ): E {
        return returnStickyCollection?.invoke(clazz, key) as? E ?: super.getSticky(clazz, key)
    }
}