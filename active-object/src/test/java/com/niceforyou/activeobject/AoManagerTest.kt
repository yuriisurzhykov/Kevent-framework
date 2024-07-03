@file:OptIn(ExperimentalCoroutinesApi::class)

package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import com.github.yuriisurzhykov.kevent.activeobject.common.EventSubscriberFilter
import com.github.yuriisurzhykov.kevent.activeobject.common.InitializationCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.communication.CollectionFlowCommunication
import com.github.yuriisurzhykov.kevent.activeobject.communication.FlowCommunicationBuilder
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManager
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManagerEventFilter
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.AoManagerStateMachine
import com.github.yuriisurzhykov.kevent.activeobject.manager.hsm.InitPhaseOneState
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import com.github.yuriisurzhykov.kevent.events.codegen.DefaultStickyEventsFactory
import com.github.yuriisurzhykov.kevent.statemachine.transition.TransitionParams
import com.niceforyou.activeobject.bus.fakes.FakePersistableEventRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class AoManagerTest {

    @Test
    fun `test events for AO manager to subscribe for`() = runTest {
        val aoManagerFilter = AoManagerEventFilter()

        val actual = aoManagerFilter.commonEventsToSubscribe
        val expected = setOf(
            SubscriptionCompleteEvent::class,
            InitPhaseTwoDone::class
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test AoManager subscribes all AOs`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val testAo = FakeActiveObject(flowBus)

        val aoManager = AoManager(setOf(testAo), flowBus, UnconfinedTestDispatcher())

        aoManager.startInitialization().join()
        //delay to be sure async tasks completed execution
        delay(100)

        // Check that subscribed AOs are aoManager itself and testAo
        val expectedSubscribers = setOf(aoManager, testAo)
        val actualSubscribers = flowBus.subscribedAos
        assertEquals(expectedSubscribers, actualSubscribers)
    }

    @Test
    fun `test AoInitializingState subscribe active objects`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val fakeAo = FakeActiveObject(flowBus)
        val state = InitPhaseOneState(setOf(fakeAo), flowBus)

        val stateMachine = AoManagerStateMachine(setOf(fakeAo), flowBus)

        state.onEnter(stateMachine.context, TransitionParams.Empty)

        delay(100)

        val expected = setOf(fakeAo)
        val actual = flowBus.subscribedAos

        assertEquals(expected, actual)
    }

    @Test
    fun `test InitPhaseOneState moves to InitPhaseTwoState when all AO ready`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val fakeAo = FakeActiveObject(flowBus)

        val aoManager = AoManager(setOf(fakeAo), flowBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        val expected = listOf(
            SubscriptionCompleteEvent(ClassSerialWrapper(fakeAo::class)),
            InitPhaseTwoDone(ClassSerialWrapper(fakeAo::class)),
            InitializationCompleteEvent
        )
        val actual = flowBus.sentEvents
        assertEquals(expected, actual)

        assertEquals(1, fakeAo.onCreateCalls)
        assertEquals(1, fakeAo.publishEventsCallCount)
    }

    @Test
    fun `test AO starts receiving events after it is initialized`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val fakeAo = FakeActiveObject(flowBus)

        val aoManager = AoManager(setOf(fakeAo), flowBus, UnconfinedTestDispatcher())
        aoManager.startInitialization()

        assertEquals(1, fakeAo.onCreateCalls)
        assertEquals(1, fakeAo.publishEventsCallCount)

        val event = FakeEvent()
        flowBus.publish(event)

        val expectedProcessed = listOf(event)
        val actualProcessed = fakeAo.processedEvents

        assertEquals(expectedProcessed, actualProcessed)
    }

    @Test
    fun `test AoManager manages initialization of 100 AO`() = runTest {
        val communication = FakeCommunication()
        val flowBus = FakeFlowBus(communication)
        val listSize = 100
        val fakeAos = List(listSize) {
            FakeActiveObject(flowBus, context = ImmediateTestDispatcher())
        }

        val aoManager = AoManager(fakeAos.toSet(), flowBus, ImmediateTestDispatcher())
        aoManager.startInitialization()

        val actualCreateCalls = fakeAos.map { it.onCreateCalls }
        val actualPublishCalls = fakeAos.map { it.publishEventsCallCount }
        val expected = List(listSize) { 1 }
        assertEquals(expected, actualCreateCalls)
        assertEquals(expected, actualPublishCalls)
    }
}

internal class FakeEvent : Event

@Suppress("UNCHECKED_CAST")
private class FakeStickyFactory : DefaultStickyEventsFactory {
    override fun <T : Event.Sticky> createDefault(kClass: KClass<T>): T {
        return defaultStickyEvent as T
    }

    override fun <T : Event.StickyCollection<*>> createDefault(kClass: KClass<T>): T {
        return defaultStickyCollectionEvent as T
    }
}

internal class FakeCommunication :
    CollectionFlowCommunication by FlowCommunicationBuilder.StickyCollection.build()

internal class FakeFlowBus(communication: CollectionFlowCommunication) : FlowBus.Abstract(
    FakeStickyFactory(),
    communication,
    FakePersistableEventRegistry(),
    FakeEventValidator()
) {

    val subscribedAos = mutableSetOf<ActiveObject>()
    val sentEvents = mutableListOf<Event>()

    override fun subscribe(activeObject: ActiveObject) {
        super.subscribe(activeObject)
        subscribedAos.add(activeObject)
    }

    override suspend fun <T : Event> publish(event: T) {
        super.publish(event)
        sentEvents.add(event)
    }
}

internal class FakeActiveObject(
    flowBus: FlowBus,
    context: CoroutineContext = UnconfinedTestDispatcher(),
    filter: EventSubscriberFilter = FakeSubscribeFilter()
) : ActiveObject(filter, flowBus, context) {

    constructor(
        flowBus: FlowBus,
        context: CoroutineContext = UnconfinedTestDispatcher(),
        eventClass: KClass<out Event>
    ) : this(flowBus, context, FakeSubscribeFilter(eventClass))

    var onCreateCalls: Int = 0
    var onDestroyCalls: Int = 0
    val processedEvents = mutableListOf<Event>()
    val processedErrors = mutableListOf<Throwable>()
    var publishEventsCallCount: Int = 0
    var subscribedCallCount: Int = 0

    override suspend fun handleError(error: Throwable) {
        processedErrors.add(error)
    }

    override suspend fun onCreated() {
        onCreateCalls++
    }

    override suspend fun onDestroy() {
        onDestroyCalls++
    }

    override suspend fun onEvent(event: Event, flowBus: FlowBus) {
        processedEvents.add(event)
    }

    override suspend fun publishInitialEvents(flowBus: EventManager) {
        publishEventsCallCount++
    }

    override suspend fun notifyActiveObjectSubscribed() {
        super.notifyActiveObjectSubscribed()
        subscribedCallCount++
    }
}