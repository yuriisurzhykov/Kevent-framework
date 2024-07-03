package com.github.yuriisurzhykov.kevent.events.codegen

/**
 * Exception is thrown when a user (developer) defined default value for an event parameter
 * that has wrong value for a defined type. For example, if a property has a type Int, and
 * developer is trying to set `"empty"` value to it, the exception will be thrown during KSP
 * compilation phase.
 * */
class IllegalDefaultFormatException(message: String?) : RuntimeException(message)