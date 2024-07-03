package com.github.yuriisurzhykov.kevent.events.codegen

/**
 * Annotation used to designate a class as a factory component for `EventBus` component instance.
 * Classes annotated with `FactoryComponent` are involved in the production of event objects,
 * typically serving as factories for creating these events. You should have only one
 * `FactoryComponent` class in your app and it always should be abstract class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StickyFactoryComponent