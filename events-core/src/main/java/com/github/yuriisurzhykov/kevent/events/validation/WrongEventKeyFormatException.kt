package com.github.yuriisurzhykov.kevent.events.validation

import com.github.yuriisurzhykov.kevent.events.Event
import kotlin.reflect.KClass

class WrongEventKeyFormatException(
    event: KClass<out Event>,
    key: Any,
    rule: ValidateEventKey<*>
) : RuntimeException("Event $event with key `$key` doesn't match requirements of the rule $rule!")