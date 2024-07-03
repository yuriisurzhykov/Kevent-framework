@file:Suppress("unused")

package com.github.yuriisurzhykov.kevent.ksp.core

import com.squareup.kotlinpoet.AnnotationSpec

/**
 * Returns an AnnotationSpec representing the `@Suppress("UNCHECKED_CAST")` annotation.
 */
fun uncheckedCast() = suppress("UNCHECKED_CAST")

/**
 * Returns an AnnotationSpec representing the `@Suppress("unused")` annotation.
 */
fun unused() = suppress("unused")

/**
 * Returns an AnnotationSpec representing the `@Suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")` annotation.
 */
fun unusedUncheckedCast() = suppress("UNCHECKED_CAST", "UNUSED_EXPRESSION")

fun suppress(vararg suppresses: String): AnnotationSpec {
    val builder = AnnotationSpec.builder(Suppress::class)
    suppresses.forEach { builder.addMember("%S", it) }
    return builder.build()
}