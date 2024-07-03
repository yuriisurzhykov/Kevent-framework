package com.github.yuriisurzhykov.kevent.events.persisted.core.dao

/**
 * This annotation is used to indicate that a class(or interface) is an automatically
 * generated Dao(data access object).
 * AutoDao annotation is used only to mark a class that need to be processed by
 * `AutoDatabaseSymbolProcessor` and should NOT be used by developers manually.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AutoDao