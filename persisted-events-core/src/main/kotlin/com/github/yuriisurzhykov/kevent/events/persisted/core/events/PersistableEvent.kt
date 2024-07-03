package com.github.yuriisurzhykov.kevent.events.persisted.core.events

import com.github.yuriisurzhykov.kevent.events.Event

/**
 * `PersistableEvent` annotation acts as a marker that is used to annotate an event that must be
 * saved to the database every time it is published.
 * During build process, a special processor will look for the classes marked with @PersistableEvent
 * annotation, and generate multiple files to persist event to a database.
 * Notice: When you annotate your class with @PersistableEvent, you have to inherit it also from
 * [Event.Persistable] interface and the event class has to be either [Event.StickyCollection] or
 * [Event.Sticky]
 * */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class PersistableEvent