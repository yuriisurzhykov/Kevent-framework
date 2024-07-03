package com.github.yuriisurzhykov.kevent.events.validation

interface ValidateEventKey<T : Any> {

    fun isValid(input: T): Boolean

    /**
     * Represents a range validation rule for integer id of any sticky collection event.
     *
     * @property minItem The minimum number of items allowed in the range.
     * @property maxItem The maximum number of items allowed in the range.
     */
    data class ItemsRange(
        private val minItem: Int,
        private val maxItem: Int
    ) : ValidateEventKey<Int> {
        override fun isValid(input: Int): Boolean {
            return input in minItem..maxItem
        }
    }

    /**
     * Represents a regular expression validation rule for the sticky collection event with
     * strings as a key.
     *
     * @property regex The regular expression pattern to match against.
     */
    data class Regex(
        private val regex: kotlin.text.Regex
    ) : ValidateEventKey<String> {
        override fun isValid(input: String): Boolean {
            return regex.matches(input)
        }
    }

    /**
     * Represents a range validation rule for integer id of any sticky collection event.
     * For example for value 20 with range of 10 through 50 with step 10 this validator
     * will return `true`.
     *
     * @property minItem The minimum number of items allowed in the range.
     * @property maxItem The maximum number of items allowed in the range.
     * @property step The increase step for input value
     */
    data class RangeWithStep(
        private val minItem: Int,
        private val maxItem: Int,
        private val step: Int
    ) : ValidateEventKey<Int> {
        override fun isValid(input: Int): Boolean {
            return input in minItem..maxItem && (input - minItem) % step == 0
        }
    }

    /**
     * Validation rule to check if input value is in the valid range of values.
     * For example values might be 12, 39, 44 for integer value and input have to be
     * only one of this values.
     *
     * @param range The range of valid values to verify the input.
     * */
    class ExactValue<T : Any>(
        private vararg val range: T
    ) : ValidateEventKey<T> {
        override fun isValid(input: T): Boolean {
            return input in range
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ExactValue<*>

            return range.contentEquals(other.range)
        }

        override fun hashCode(): Int {
            return range.contentHashCode()
        }

        override fun toString(): String {
            return "ExactValue(range=${range.contentToString()})"
        }
    }
}