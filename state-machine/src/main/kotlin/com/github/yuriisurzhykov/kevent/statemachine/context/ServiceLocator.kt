package com.github.yuriisurzhykov.kevent.statemachine.context

/**
 *  ServiceLocator is a pattern that provides dependencies to every part of an application.
 *  In short, it creates instances for the interfaces and provides them to everyone who is
 *  interested in them. Because each state machine may use different UseCases and different
 *  util classes, this [ServiceLocator] provides all the necessary dependencies.
 *  [ServiceLocator] is just an empty interface that provides nothing by default.
 *  To use this class, you have to create your own interface and inherit it from
 *  this one.
 *
 *  __For example__. Let's say you have to make if-else condition check in your state to do
 *  transition. That logic might depends on different factors or different components of application
 *  (whether it events, DAOs, whatever). So instead of coupling all parts of application service
 *  locator provides ability to avoid tight coupling between different parts of application.
 *  So instead of having problems about how to get DAO inside of state, or instead of having
 *  all hard-coded logic inside of state, we are wrapping it as use case, and provides use cases
 *  from service locator. In this way instead of having the next code that we cannot test:
 *  ```
 *  class BadState: State.Normal(SomeParent) {
 *
 *      override fun processEvent(event: Event): ProcessResult {
 *          return if (event is SomeEvent) {
 *              val stickyEvent = EventBus.getSticky(SomeEventGroup.Event::class)
 *              val isMatchesCondition = stickyEvent.data == "SomeValue"
 *              if (isMatchesCondition) transitionTo(AnotherState)
 *              else unhandled(event)
 *          } else unhandled(event)
 *      }
 *  }
 *  ```
 *  We have the following logic.
 *  ```
 *  class GoodState: State.Normal(SomeParent) {
 *
 *      override fun processEvent(event: Event, context: StateMachineContext): ProcessResult {
 *          return if (event is SomeEvent) {
 *              if (isMatchesCondition()) transitionTo(AnotherState)
 *              else unhandled(event)
 *          } else unhandled(event)
 *      }
 *
 *      private fun matchesCondition(context: StateMachineContext): Boolean {
 *          return serviceLocator<CertainServiceLocator>(context)
 *              .someClassToProvide()
 *              .matchesConditions()
 *      }
 *  }
 *
 *  interface SomeClassToProvide {
 *      fun matchesConditions(): Boolean
 *      class Base: SomeClassToProvide {
 *          override fun matchesCondition() = EventBus.getSticky(Event::class).data == "SomeValue"
 *      }
 *  }
 *
 *  interface CertainServiceLocator : ServiceLocator {
 *      fun someClassToProvide(): SomeClassToProvide
 *
 *      class Base : CertainServiceLocator {
 *          override fun someClassToProvide(): SomeClassToProvide = SomeClassToProvide.Base()
 *      }
 *  }
 *  ```
 *  Yes, its a more code, it a little more files, but the benefits of using this are much
 *  greater rather than writing code with imperative approach.
 *
 *  For deeper understanding why service locator is so useful here is key aspects of it:
 *  - __Decoupling__: It facilitates decoupling by separating the creation and retrieval of service
 *  instances from their usage. This approach enhances maintainability and clarity in the codebase,
 *  as classes using these services are not burdened with the details of their creation or lifecycle
 *  management.
 *
 *  - __Flexibility and Testability__: By centralizing service access, it allows for more flexibility
 *  in providing different implementations (such as mocks for testing) of these services, enhancing
 *  testability.
 *
 *  - __Specificity of Services__: Unlike auto-generated getters and setters, the methods in this
 *  interface are specifically tailored to the needs of the alarm system, ensuring that the right
 *  services are provided and managed in a way that aligns with the system's requirements.
 *
 *  - __Not Auto-Generatable__: The logic and instances provided by any method defined in  service
 *  locator interface is unique to the specific AO needs and cannot be generated automatically
 *  by Kotlin. They require deliberate definition to align with the system's architecture and
 *  operational demands.
 * */
interface ServiceLocator {

    /**
     *  If you don't need service locator for your state machine, you may use this dummy class,
     *  to provide it as a Service Locator. But you have to be aware, if you really don't need
     *  service locator and you don't keep any business logic inside of states.
     * */
    @Suppress("unused")
    class Empty : ServiceLocator
}