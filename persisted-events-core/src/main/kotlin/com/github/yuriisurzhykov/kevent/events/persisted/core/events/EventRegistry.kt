package com.github.yuriisurzhykov.kevent.events.persisted.core.events

/**
 * Annotation class used to mark an abstract class or interface as an event registry.
 * The class/interface, annotated @EventRegistry have to implement also [PersistableEventRegistry]
 * interface. Read more in [PersistableEventRegistry].
 */
annotation class EventRegistry