package com.github.yuriisurzhykov.kevent.events.codegen

/**
 * Annotation used to designate a class as a component of the Sticky event system in the FlowBus
 * library. Classes annotated with `DefaultableStickyEvent` are recognized as part of the sticky event
 * handling mechanism, typically for the purpose of generating default values in generated factory
 * [DefaultStickyEventsFactory] for sticky events.
 */
@Target(AnnotationTarget.CLASS)         // Indicates that this annotation is applicable only to classes.
@Retention(AnnotationRetention.RUNTIME) // The annotation will be available at compilation & runtime.
annotation class DefaultableStickyEvent
