package com.niceforyou.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.common.DisposeObjects
import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.niceforyou.activeobject.FakeCommunication
import com.niceforyou.activeobject.FakeEvent
import com.niceforyou.activeobject.FakeFlowBus
import com.niceforyou.activeobject.TestActiveObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveObjectTest {

    @Test
    fun `check call publish initial events`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        activeObject.doInternalInitialization()

        assertEquals(1, activeObject.publishInitialEventsCalled)

        val sentEvent = flowBus.sentEvents[0]
        val expected = InitPhaseTwoDone(ClassSerialWrapper(activeObject::class))

        assertEquals(expected, sentEvent)
    }

    @Test
    fun `check onEvent is being called for every event AO is subscribed`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        filter.event = FakeEvent::class

        val expected = FakeEvent()
        flow.emit(expected)

        val actual = activeObject.receivedEvents[0]

        assertEquals(expected, actual)
    }

    @Test
    fun `check onCreated is being called after InitializationCompleteEvent is sent`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        flow.emit(InitializationCompleteEvent)

        assertEquals(1, activeObject.onCreatedCalled)
    }

    @Test
    fun `check onDestroy is being called when Dispose event occur`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        flow.emit(DisposeObjects)

        assertEquals(1, activeObject.onDestroyedCalled)
    }

    @Test
    fun `check don't process events when Dispose event occur`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        flow.emit(DisposeObjects)

        flow.emit(FakeEvent())

        assertEquals(1, activeObject.onDestroyedCalled)
        assertEquals(emptyList<Event>(), activeObject.receivedEvents)
    }

    @Test
    fun `check AO receives events after subscribeTo and startProcessing called`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        val expected = List(100) { FakeEvent() }
        expected.forEach { flow.emit(it) }

        assertEquals(expected, activeObject.receivedEvents)
    }

    @Test
    fun `check SubscriptionCompleteEvent is sent after subscribeTo is called`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)

        val expected = SubscriptionCompleteEvent(ClassSerialWrapper(activeObject::class))
        val actual = flowBus.sentEvents[0]

        assertEquals(expected, actual)
    }

    @Test
    fun `check call filter function on each event occurrence`() = runTest {
        val filter = FakeFilter()
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(filter, flowBus)

        val flow = MutableSharedFlow<Event>()
        activeObject.subscribeTo(flow)
        activeObject.startEventProcessing()

        val repeatAmount = 100

        repeat(repeatAmount) {
            flow.emit(FakeEvent())
        }

        assertEquals(repeatAmount, filter.allowedProcessCallCount)
    }

    @Test
    fun `check AO processes InitializationCompleteEvent and DisposeObjects events`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val activeObject = TestActiveObject(flowBus)

        flowBus.subscribe(activeObject)
        activeObject.startEventProcessing()

        flowBus.publish(InitializationCompleteEvent)
        flowBus.publish(DisposeObjects)

        assertTrue(activeObject.initialized)
        assertTrue(activeObject.disposed)
    }

    private open class FakeFilter : EventSubscriberFilter.Base() {
        var allowedProcessCallCount: Int = 0
        var event: KClass<out Event> = FakeEvent::class
        override val commonEventsToSubscribe: Set<KClass<out Event>>
            get() = setOf(event)

        override fun allowToProcess(event: Event): Boolean {
            allowedProcessCallCount++
            return super.allowToProcess(event)
        }
    }

    private class TestActiveObject(
        filter: EventSubscriberFilter,
        flowBus: FlowBus,
        scope: CoroutineContext = UnconfinedTestDispatcher()
    ) : ActiveObject(filter, flowBus, scope) {

        var onCreatedCalled = 0
        var onDestroyedCalled = 0
        var handledError: Throwable? = null
        var notifySubscribedCalled = 0
        var publishInitialEventsCalled = 0
        val receivedEvents = mutableListOf<Event>()
        var onEvent: (Event) -> Unit = { event ->
            receivedEvents.add(event)
        }

        override suspend fun onCreated() {
            onCreatedCalled++
            super.onCreated()
        }

        override suspend fun onDestroy() {
            onDestroyedCalled++
            super.onDestroy()
        }

        override suspend fun handleError(error: Throwable) {
            handledError = error
            super.handleError(error)
        }

        override suspend fun notifyActiveObjectSubscribed() {
            notifySubscribedCalled++
            super.notifyActiveObjectSubscribed()
        }

        override suspend fun publishInitialEvents(flowBus: EventManager) {
            publishInitialEventsCalled++
        }

        override suspend fun onEvent(event: Event, flowBus: FlowBus) {
            onEvent.invoke(event)
        }
    }
}