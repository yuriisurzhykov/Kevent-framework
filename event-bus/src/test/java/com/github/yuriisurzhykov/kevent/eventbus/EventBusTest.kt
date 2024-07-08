package com.github.yuriisurzhykov.kevent.eventbus

import com.github.yuriisurzhykov.kevent.eventbus.fakes.FakePersistableEventRegistry
import com.github.yuriisurzhykov.kevent.eventbus.communication.CollectionFlowCommunication
import com.github.yuriisurzhykov.kevent.eventbus.fakes.FakeEventBusCommunication
import com.github.yuriisurzhykov.kevent.eventbus.fakes.FakeStickyComponentsFactory
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.EventNotValidException
import com.github.yuriisurzhykov.kevent.events.NoStickyValueFoundException
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import com.github.yuriisurzhykov.kevent.events.validation.WrongEventKeyFormatException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EventBusTest {

    @Test(expected = EventNotValidException::class)
    fun `check throws error if validation not success for validatable event`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val event = FakeValidatable()

        eventBus.publish(event)
    }

    @Test(expected = WrongEventKeyFormatException::class)
    fun `check throws exception if key not valid for event`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = EventValidator.ValidatorsRegistry()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val event = FakeKeyValidatable(13)

        eventBus.publish(event)
    }

    @Test
    fun `check that emit method of FlowCommunication is called`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val event = TestEvent()

        eventBus.publish(event)

        val actual = communication.emittedList.size
        val expected = 1

        assertEquals(expected, actual)
    }

    @Test
    fun `check persist event which is Persistable`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val event = FakePersistable()

        eventBus.publish(event)

        val actual = persistableRegistry.calledPersistCount
        val expected = 1

        assertEquals(expected, actual)
    }

    @Test
    fun `check sticky event is being returned after it has been sent`() = runTest {
        val communication =
            CollectionFlowCommunication.Base(HashMap(), HashMap(), HashMap(), MutableSharedFlow())
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyEvent(12)
        eventBus.publish(expected)

        val actual = eventBus.getSticky(FakeStickyEvent::class)

        assertEquals(expected, actual)
    }

    @Test
    fun `check persisted event returned if no runtime sticky event`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyEvent(12)
        persistableRegistry.fakePersistedSticky = FakeStickyEvent(12)
        communication.returnSticky = { throw NoStickyValueFoundException(FakeStickyEvent::class) }

        val actual = eventBus.getSticky(FakeStickyEvent::class)

        assertEquals(expected, actual)
        assertEquals(1, persistableRegistry.readStickyCallCount)
    }

    @Test
    fun `check default event is returned if no runtime or persisted`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyEvent(12)
        persistableRegistry.fakePersistedSticky = null
        factory.defaultSticky = expected
        communication.returnSticky = { throw NoStickyValueFoundException(FakeStickyEvent::class) }

        val actual = eventBus.getSticky(FakeStickyEvent::class)

        assertEquals(expected, actual)
        assertEquals(1, persistableRegistry.readStickyCallCount)
        assertEquals(1, factory.produceStickyCallCount)
    }

    @Test
    fun `check returns emitted sticky collection event`() = runTest {
        val communication =
            CollectionFlowCommunication.Base(HashMap(), HashMap(), HashMap(), MutableSharedFlow())
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus = EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyCollection(1)
        eventBus.publish(expected)

        val actual = eventBus.getSticky(FakeStickyCollection::class, 1)

        assertEquals(expected, actual)
        assertEquals(0, persistableRegistry.readStickyCollectionCallCount)
        assertEquals(0, factory.produceStickyCollectionCallCount)
    }

    @Test
    fun `check returns persisted sticky collection if no runtime event`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyCollection(1)
        persistableRegistry.fakePersistedStickyCollection = expected
        communication.returnStickyCollection =
            { _, _ -> throw NoStickyValueFoundException(FakeStickyCollection::class) }

        val actual = eventBus.getSticky(FakeStickyCollection::class, 1)

        assertEquals(expected, actual)
        assertEquals(1, persistableRegistry.readStickyCollectionCallCount)
        assertEquals(0, factory.produceStickyCollectionCallCount)
    }

    @Test
    fun `check creates default sticky collection if no runtime or persisted`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus =
            EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val expected = FakeStickyCollection(12)
        persistableRegistry.fakePersistedStickyCollection = null
        communication.returnStickyCollection =
            { _, _ -> throw NoStickyValueFoundException(FakeStickyCollection::class) }
        factory.defaultStickyCollection = FakeStickyCollection(12)

        val actual = eventBus.getSticky(FakeStickyCollection::class, 12)

        assertEquals(expected, actual)
        assertEquals(1, persistableRegistry.readStickyCollectionCallCount)
        assertEquals(1, factory.produceStickyCollectionCallCount)
    }

    @Test
    fun `check deletes sticky if exists`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus = EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        eventBus.delete(FakeStickyEvent::class)

        assertEquals(1, communication.deleteStickyCount)
        assertEquals(1, persistableRegistry.deleteStickyCount)
    }

    @Test
    fun `check deletes sticky collection if exists`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus = EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        eventBus.delete(FakeStickyCollection::class, 1)

        assertEquals(1, communication.deleteStickyCollectionCount)
        assertEquals(1, persistableRegistry.deleteStickyCollectionCount)
    }

    @Test
    fun `check fetches both persistable and runtime sources for sticky collection`() = runTest {
        val communication = FakeEventBusCommunication()
        val factory = FakeStickyComponentsFactory()
        val eventValidator = FakeEventValidator()
        val persistableRegistry = FakePersistableEventRegistry()
        val eventBus = EventBus.Base(factory, communication, persistableRegistry, eventValidator)

        val collection = FakeStickyCollection(1)
        val collection2 = FakeStickyCollection(2)
        communication.emittedStickyCollection = mutableMapOf(
            (FakeStickyCollection::class to collection)
        )
        persistableRegistry.fakePersistedStickyCollection = collection2

        val actual = eventBus.getCollection(FakeStickyCollection::class)
        val expected = setOf(collection, collection2)

        assertEquals(expected, actual)
    }

    private class FakeValidatable : Event, Event.Validatable {
        override suspend fun validate(eventManager: EventManager): EventNotValidException {
            return object : EventNotValidException() {}
        }
    }

    private data class FakeKeyValidatable(
        override val key: Int
    ) : Event.KeyValidatable<Int> {
        override fun validationRule(): ValidateEventKey<Int> {
            return ValidateEventKey.ItemsRange(1, 3)
        }
    }

    private data class FakeStickyEvent(
        val instance: Int
    ) : Event.Sticky()

    private data class FakeStickyCollection(
        override val key: Int
    ) : Event.StickyCollection<Int>()

    private class FakePersistable : Event, Event.Persistable
}