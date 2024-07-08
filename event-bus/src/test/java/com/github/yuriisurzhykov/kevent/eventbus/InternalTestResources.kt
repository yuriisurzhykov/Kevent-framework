package com.github.yuriisurzhykov.kevent.eventbus

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import kotlin.reflect.KClass

internal class TestEvent : Event

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