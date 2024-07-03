package com.niceforyou.events

import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegexEventValidationTest {

    @Test
    fun `test returns true when string matches regex`() {
        val rule = ValidateEventKey.Regex("g(oog)+le".toRegex())
        val testSource = arrayOf("google", "googoogle", "googoogoogle", "googoogoogoogle")
        for (test in testSource) {
            assertTrue(rule.isValid(test))
        }
    }

    @Test
    fun `test returns false when string doesn't match regex`() {
        val rule = ValidateEventKey.Regex("g(oog)+le".toRegex())
        val testSource = arrayOf("asdasd", "ffwwf", "fqfqfqf", "qrqrqrqrq")
        for (test in testSource) {
            assertFalse(rule.isValid(test))
        }
    }
}