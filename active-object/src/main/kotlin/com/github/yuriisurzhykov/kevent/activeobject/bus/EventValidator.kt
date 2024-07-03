package com.github.yuriisurzhykov.kevent.activeobject.bus

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.validation.WrongEventKeyFormatException
import com.github.yuriisurzhykov.kevent.events.validation.ValidateEventKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * The EventValidator interface provides methods for validating events according to specific rules.
 */
interface EventValidator {

    /**
     * Validates an event by checking its validity according to the provided validation rule.
     *
     * @param event The event to validate.
     * @return true if the event is valid, false otherwise.
     */
    suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(event: E)

    /**
     * Validates an event by checking its validity according to the provided validation rule.
     *
     * @param clazz The class type of the event to validate.
     * @param key The key value of the class to be validated.
     * @return true if the event is valid, false otherwise.
     */
    suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(clazz: KClass<E>, key: K)

    class ValidatorsRegistry(
        private val mutexMap: HashMap<KClass<out Event>, Mutex> = HashMap(),
        private val validatorMap: HashMap<KClass<out Event>, ValidateEventKey<out Any>?> = HashMap()
    ) : EventValidator {

        /**
         * Validates an event by checking its validity according to the provided validation rule.
         *
         * @param event The event to validate.
         * @return true if the event is valid, false otherwise.
         * @throws WrongEventKeyFormatException if the event doesn't match the requirements of the rule.
         */
        override suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(event: E) {
            val rule = mutexMap.getOrPut(event::class) { Mutex() }.withLock {
                validatorMap[event::class] = event.validationRule()
                event.validationRule()
            }
            if (rule != null && !rule.isValid(event.key)) {
                throw WrongEventKeyFormatException(event::class, event.key, rule)
            }
        }

        /**
         * Validates an event by checking its validity according to the provided validation rule.
         *
         * @param clazz The class of the event to validate.
         * @param key The unique identifier of the event instance.
         * @return true if the event is valid, false otherwise.
         * @throws WrongEventKeyFormatException if the event doesn't match the requirements of the rule.
         */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <K : Any, E : Event.KeyValidatable<K>> validateOrThrow(
            clazz: KClass<E>,
            key: K
        ) {
            val rule = mutexMap.getOrPut(clazz) { Mutex() }.withLock {
                return@withLock (validatorMap[clazz] as? ValidateEventKey<K>)
            }
            if (rule != null && !rule.isValid(key)) {
                throw WrongEventKeyFormatException(clazz, key, rule)
            }
        }
    }
}