package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import com.github.yuriisurzhykov.kevent.eventbus.EventValidator
import com.github.yuriisurzhykov.kevent.eventbus.EventBus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

internal class ImmediateTestDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}

internal class TestEvent : Event

@OptIn(ExperimentalCoroutinesApi::class)
internal class TestActiveObject(
    eventBus: EventBus,
    filter: EventSubscriberFilter = FakeSubscribeFilter(TestEvent::class)
) : ActiveObject(filter, eventBus, UnconfinedTestDispatcher()) {

    var processCallAmount: Int = 0
    val processedEvents = mutableListOf<Event>()
    val processedErrors = mutableListOf<Throwable>()
    var initialized: Boolean = false
    var disposed: Boolean = false

    var processEvent: suspend (Event) -> Unit = {
        processCallAmount++
        processedEvents.add(it)
    }

    override suspend fun onCreated() {
        initialized = true
    }

    override suspend fun onDestroy() {
        disposed = true
    }

    override suspend fun onEvent(event: Event, eventBus: EventBus) {
        processEvent.invoke(event)
    }

    override suspend fun handleError(error: Throwable) {
        processedErrors.add(error)
    }

    override suspend fun publishInitialEvents(eventManager: EventManager) {

    }
}

internal data class TestStickyEvent(val id: Int = 0) : Event.Sticky()

internal data class TestStickyCollection(
    override val key: Int
) : Event.StickyCollection<Int>() {

    var validationRule: ValidateEventKey<Int>? = null
    override fun validationRule() = validationRule
}

internal class FakeEventValidator : EventValidator {

    var validation: () -> Unit = { }

    override suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(event: E) =
        validation.invoke()

    override suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(
        clazz: KClass<E>,
        key: K
    ) = validation.invoke()
}

internal val defaultStickyEvent = TestStickyEvent(-1)
internal val defaultStickyCollectionEvent = TestStickyCollection(1)
