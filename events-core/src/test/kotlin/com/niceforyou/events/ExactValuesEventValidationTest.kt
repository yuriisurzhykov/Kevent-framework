package com.niceforyou.events

import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactValuesEventValidationTest {

    @Test
    fun `check returns true if input in valid integer values array`() {
        val testValues = arrayOf(4, 13, 402)
        val rule = ValidateEventKey.ExactValue(*testValues)

        for (input in testValues) {
            val actual = rule.isValid(input)
            assertTrue("Validation failed for value $input! Should return true", actual)
        }
    }

    @Test
    fun `check returns false if input not in valid integer values array`() {
        val testValues = arrayOf(4, 13, 402)
        val rule = ValidateEventKey.ExactValue(*testValues)

        val actual = rule.isValid(124)
        assertFalse("Validation failed! Should return true", actual)
    }

    @Test
    fun `check returns true if input in valid custom class values array`() {
        val testValues = arrayOf(FakeDataClass(21), FakeDataClass(42), FakeDataClass(301))
        val rule = ValidateEventKey.ExactValue(*testValues)

        for (input in testValues) {
            val actual = rule.isValid(input)
            assertTrue("Validation failed for value $input! Should return true", actual)
        }
    }

    @Test
    fun `check returns false if input not in valid custom class values array`() {
        val testValues = arrayOf(FakeDataClass(21), FakeDataClass(42), FakeDataClass(301))
        val rule = ValidateEventKey.ExactValue(*testValues)

        val actual = rule.isValid(FakeDataClass(124))
        assertFalse("Validation failed! Should return true", actual)
    }

    private data class FakeDataClass(
        val someValue: Int
    )
}