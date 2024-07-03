package com.github.yuriisurzhykov.kevent.activeobject.common

import com.github.yuriisurzhykov.kevent.activeobject.bus.FlowBus
import com.github.yuriisurzhykov.kevent.activeobject.manager.AoManager
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.ClassSerialWrapper
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.InitPhaseTwoDone
import com.github.yuriisurzhykov.kevent.activeobject.manager.events.SubscriptionCompleteEvent
import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 *  Abstract class representing an active object that listens to and processes events.
 *
 *  @property eventFilter A filter containing the classes that Active Object should subscribe to.
 *  @property flowBus The FlowBus instance for event communication.
 *  @property coroutineContext The coroutine context on which this active object operates. This
 *  property created to add more flexibility to test AO logic and for further modifications if they
 *  require.
 *  @property name Optional name for the active object, defaults to the class name if not provided.
 */
abstract class ActiveObject(
    private val eventFilter: EventSubscriberFilter,
    private val flowBus: FlowBus,
    private val coroutineContext: CoroutineContext = AoCoroutineContext(),
    private val name: String? = null
) {

    // The kotlin coroutines Job interface that is parent for all subprocesses within AO
    private val supervisorJob: Job = SupervisorJob()
    private val activeObjectName: String? = name ?: this::class.simpleName

    // The kotlin coroutines channel that consumes events from FlowBus
    private val eventQueue: Channel<Event> = Channel(Channel.UNLIMITED)

    // Indicator for active object to not be initialized more then 1 time.
    private val hasInitialized: AtomicBoolean = AtomicBoolean(false)

    private val internalSubscribeEvents = setOf(
        InitializationCompleteEvent::class,
        DisposeObjects::class
    )

    // The main component of ActiveObject. All tasks within one AO are executed on this scope.
    // By using AoCoroutineContext() function it allocated dedicated thread for actual AO.
    protected val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext + supervisorJob)

    /**
     *  Publishes initial events when the active object is ready. Called by [AoManager]
     *  when all AOs sent [SubscriptionCompleteEvent]
     *  Must be implemented by derived classes to publish class-related events.
     */
    protected abstract suspend fun publishInitialEvents(flowBus: EventManager)

    /**
     *  Handles incoming events.
     *  Must be implemented by derived classes to define custom event processing logic.
     *
     *  @param event The event to be processed.
     */
    protected abstract suspend fun onEvent(event: Event, flowBus: FlowBus)

    /**
     *  Represents one of AO's lifecycle stages. Called once AO subscribed for receiving events
     *  and published all AO-related events during initialization.
     * */
    protected open suspend fun onCreated() {}

    /**
     *  Represents one of AO's lifecycle stages. Called once AO disposed all resources and intending
     *  to cancel all jobs on current [coroutineScope]. If you create a new coroutine job in this
     *  function it will be cancelled immediately, so you are not able to do asynchronous operation
     *  on this call.
     * */
    protected open suspend fun onDestroy() {}

    /**
     *  Function to handle all error that might occur when actual active object is running.
     *  @param error represents error that just occurred.
     *  @throws Throwable if any of occur when processing event or if something happened before
     *  event came to the event queue.
     * */
    protected open suspend fun handleError(error: Throwable) {
        throw error
    }

    /**
     *  Internal function to subscribe on [FlowBus]'s event flow. It is not accessible from outside
     *  of module `:activeObject`. So only internal resources have access to this function.
     * */
    internal fun subscribeTo(flow: SharedFlow<Event>) {
        if (!hasInitialized.getAndSet(true)) {
            coroutineScope.launch {
                // Subscribes for events on the provided coroutine scope,
                // and sends the event to event queue.
                flow
                    .onEach { event -> sendEventToQueue(event) }
                    .catch { error -> handleError(error) }
                    .launchIn(coroutineScope)
                notifyActiveObjectSubscribed()
            }
        } else throw IllegalStateException("Active Object $activeObjectName is already initialized!")
    }

    /**
     *  Logic to notify whoever listens [SubscriptionCompleteEvent]. It is also internal function
     *  that is visible only within `:activeObject` module.
     *  [AoManager] overrides this function to prevent posting any additional events.
     * */
    internal open suspend fun notifyActiveObjectSubscribed() {
        // Wrapping up class to ClassSerialWrapper because of serialization issue of kclass
        val classReference = ClassSerialWrapper(this@ActiveObject::class)
        // Notifies AoManager about this AO is ready to publish sticky events
        flowBus.publish(SubscriptionCompleteEvent(classReference))
    }

    /**
     *  This method launches background job to publish all initial events. After AO published
     *  events it sends [InitPhaseTwoDone] event to notify [AoManager] about finishing AO's job.
     * */
    internal fun doInternalInitialization() {
        coroutineScope.launch {
            publishInitialEvents(flowBus)
            val classReference = ClassSerialWrapper(this@ActiveObject::class)
            // Notifies AoManager about this AO is ready to publish sticky events
            flowBus.publish(InitPhaseTwoDone(classReference))
        }
    }

    /**
     *  Sends event for processing only if queue is not closed and event class
     *  matches one of classes from [eventFilter].
     * */
    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun sendEventToQueue(event: Event) {
        if (!eventQueue.isClosedForSend) {
            if (eventFilter.allowToProcess(event) || event::class in internalSubscribeEvents) {
                eventQueue.send(event)
            }
        }
    }

    /**
     *  Launches infinite loop for processing event from [eventQueue] with its own [Job].
     *  Loop can be interrupted for all AOs by sending [DisposeObjects] event.
     * */
    internal fun startEventProcessing() = coroutineScope.launch(Job()) {
        eventQueue.consumeEach { event ->
            try {
                processEvent(event)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     *  Internal logic for processing events. It handles [InitPhaseTwoDone] and
     *  [DisposeObjects] events in addition to events from [eventFilter] and performs related
     *  logic for these events.
     * */
    private suspend fun processEvent(event: Event) = when (event) {
        is InitializationCompleteEvent -> {
            // Notify derived classes that current active object is created
            // and initialized.
            onCreated()
        }

        is DisposeObjects -> dispose()
        else                           -> onEvent(event, flowBus)
    }

    /**
     *  Disposes active object and its resources. Closes [eventQueue] for receiving events and
     *  cancelling all background coroutines [Job] related to current active object.
     * */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun dispose() {
        // Mark channel as closed for new event. All present events in channel will be processed.
        eventQueue.close()

        // Launch coroutines to wait for remaining events in queue.
        coroutineScope.launch {
            // Wait until queue becomes available for destroy.
            // Additional check !isClosedForReceive required because isEmpty might return false
            // when channel is closed for new events.
            while (!eventQueue.isEmpty && !eventQueue.isClosedForReceive) {
                delay(100) // small delay to reduce the load
            }

            // Notifying current AO that it will be removed
            onDestroy()

            // After processing all of events, cancel remains coroutines.
            supervisorJob.cancel()
        }
    }
}