package com.niceforyou.events

import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeWithStepEventValidationTest {

    @Test
    fun `check returns true if value is even and in range`() {
        val rule = ValidateEventKey.RangeWithStep(4, 6, 2)
        for (input in arrayOf(4, 6)) {
            val actual = rule.isValid(input)
            assertTrue("Validation failed for value $input! Should return true", actual)
        }
    }

    @Test
    fun `check returns false if value is even but not in range`() {
        val rule = ValidateEventKey.RangeWithStep(4, 6, 2)
        val actual = rule.isValid(8)
        assertFalse("Validation failed! Should return false", actual)
    }

    @Test
    fun `check return true if value is odd but in range`() {
        val rule = ValidateEventKey.RangeWithStep(25, 45, 10)
        for (input in arrayOf(25, 35, 45)) {
            val actual = rule.isValid(input)
            assertTrue("Validation failed for value $input! Should return true", actual)
        }
    }

    @Test
    fun `check return true if value is in range with fuzzy step`() {
        val rule = ValidateEventKey.RangeWithStep(14, 63, 7)
        for (input in arrayOf(14, 21, 28, 35, 42, 49, 56, 63)) {
            val actual = rule.isValid(input)
            assertTrue("Validation failed for value $input! Should return true", actual)
        }
    }

    @Test
    fun `check return false if value is odd and not in range`() {
        val rule = ValidateEventKey.RangeWithStep(25, 45, 10)

        val actual = rule.isValid(20)
        assertFalse("Validation failed! Should return false", actual)
    }
}