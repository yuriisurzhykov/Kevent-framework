package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators

import com.squareup.kotlinpoet.FileSpec

/**
 * The `FileGenerator` interface represents a class responsible for generating files.
 */
interface FileGenerator {

    fun generate(): FileSpec
}