package com.github.yuriisurzhykov.kevent.EventBus.validator

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import com.github.yuriisurzhykov.kevent.events.validation.WrongEventKeyFormatException
import com.github.yuriisurzhykov.kevent.eventbus.EventValidator
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EventValidatorRegistryTest {

    @Test
    fun `when validating a valid event`() = runTest {
        // Given
        val validator = EventValidator.ValidatorsRegistry()
        val event = FakeCollectionEvent(1)

        // When
        event.validationRule = ValidateEventKey.ItemsRange(1, 4)
        validator.validateOrThrow(event)
    }

    @Test(expected = WrongEventKeyFormatException::class)
    fun `when validating an invalid event`() = runTest {
        // Given
        val validator = EventValidator.ValidatorsRegistry()
        val event = FakeCollectionEvent(1)

        // When
        event.validationRule = ValidateEventKey.ItemsRange(5, 12)
        validator.validateOrThrow(event)

        // Then handle exception
    }

    @Test
    fun `when validating a valid event and key`() = runTest {
        // Given
        val validator = EventValidator.ValidatorsRegistry()
        val event = FakeCollectionEvent(1)

        // When
        event.validationRule = ValidateEventKey.ItemsRange(1, 4)
        validator.validateOrThrow(event)
        validator.validateOrThrow(event::class, 1)
    }

    @Test(expected = WrongEventKeyFormatException::class)
    fun `when validating a invalid event and key`() = runTest {
        // Given
        val validator = EventValidator.ValidatorsRegistry()
        val event = FakeCollectionEvent(1)

        // When
        event.validationRule = ValidateEventKey.ItemsRange(1, 4)
        validator.validateOrThrow(event)
        validator.validateOrThrow(event::class, 6)
    }

    private data class FakeCollectionEvent(
        val instanceId: Int
    ) : Event.StickyCollection<Int>() {
        var validationRule: ValidateEventKey<Int>? = null

        override val key = instanceId
        override fun validationRule() = validationRule
    }
}