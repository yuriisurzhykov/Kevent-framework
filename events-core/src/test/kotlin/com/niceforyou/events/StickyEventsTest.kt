package com.niceforyou.events

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.api.EventManager
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class StickyEventsTest {

    @Test
    fun `check Validatable returns null for exception by default`() = runTest {
        val event = object : Event.Validatable {}
        val eventManager = mockk<EventManager>(relaxed = true)
        val actual = event.validate(eventManager)
        assertNull(actual)
    }

    @Test
    fun `check Sticky returns null for validation rule by default`() = runTest {
        val event = object : Event.StickyCollection<Int>() {
            override val key: Int = 12
        }
        val actual = event.validationRule()
        assertNull(actual)
    }
}