package com.github.yuriisurzhykov.kevent.events.persisted.ksp.utils

import androidx.room.Embedded
import com.squareup.kotlinpoet.AnnotationSpec

/**
 * Returns an [AnnotationSpec] object representing the `@Embedded` annotation.
 */
fun embedded(): AnnotationSpec =
    AnnotationSpec.builder(Embedded::class).build()