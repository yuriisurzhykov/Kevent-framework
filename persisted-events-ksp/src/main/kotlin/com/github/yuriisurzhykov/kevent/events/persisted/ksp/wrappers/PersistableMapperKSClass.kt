package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers

import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry.RegistryFunctionBuilder.Companion.INDENT
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Represents a wrapper for event class declaration to use to generate a mapper class for persistable entity.
 *
 * @property entityWrapper An instance of [PersistableEntityKSClass] that provides information about the entity class.
 * @property packageName The package name of the mapper class.
 * @property mapperActualClassName The actual class name of the mapper class.
 */
class PersistableMapperKSClass(
    declaration: KSClassDeclaration
) {

    val entityWrapper = PersistableEntityKSClass(declaration)
    val packageName = declaration.toClassName().packageName
    val mapperActualClassName = entityWrapper.mapperActualClassName

    /**
     * Generates a [CodeBlock] that maps property names to use in `fromEntity` mapper's method.
     *
     * @param prepend The string to prepend to each mapped property name.
     * @return The generated [CodeBlock] object.
     */
    fun mapFromNames(prepend: String): CodeBlock {
        val codeBlock = CodeBlock.builder()
        entityWrapper.primaryProperties.forEachIndexed { index, property ->
            codeBlock.add("\n%L%L", INDENT, property.asMapperFromEntityStatement(prepend))
            if (index < entityWrapper.entityProperties.toList().size - 1) {
                codeBlock.add(",")
            }
        }
        return codeBlock.add("\n").build()
    }

    /**
     * Maps the properties of the [PersistableMapperKSClass] to names and returns a [CodeBlock].
     * Used for mapper generator to generate `toEntity` method.
     *
     * @param prepend The string to prepend to the property names.
     * @return A [CodeBlock] containing the mapped property names.
     */
    fun mapToNames(prepend: String): CodeBlock {
        val codeBlock = CodeBlock.builder()
        entityWrapper.entityProperties.forEachIndexed { index, property ->
            codeBlock.add("\n%L%L", INDENT, property.asMapperToEntityStatement(prepend))
            if (index < entityWrapper.entityProperties.toList().size - 1) {
                codeBlock.add(",")
            }
        }
        if (!entityWrapper.hasDefinedPrimaryKey()) {
            codeBlock.add(",\n%L1", INDENT)
        }
        return codeBlock.add("\n").build()
    }
}