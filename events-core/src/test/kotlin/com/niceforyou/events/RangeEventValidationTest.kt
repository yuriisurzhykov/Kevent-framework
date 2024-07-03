package com.niceforyou.events

import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeEventValidationTest {

    @Test
    fun `test returns true when input in range`() {
        val rule = ValidateEventKey.ItemsRange(0, 10)
        for (i in 0..10) {
            assertTrue(rule.isValid(i))
        }
    }

    @Test
    fun `test returns false when input not in range`() {
        val rule = ValidateEventKey.ItemsRange(0, 10)
        for (i in 11..23) {
            assertFalse(rule.isValid(i))
        }
    }
}