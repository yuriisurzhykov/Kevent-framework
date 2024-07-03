package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.github.yuriisurzhykov.kevent.ksp.core.containsSuperType
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

/**
 * Represents an abstract Symbol Processor that provides common functionality for processing
 * symbols (classes) with specific annotations.
 *
 * @param logger The logger used to log errors and messages during processing.
 */
abstract class AbstractSymbolProcessor(
    protected val logger: KSPLogger
) : SymbolProcessor {

    init {
        globalLogger = logger
    }

    /**
     * Checks if a class declaration has a required type(inherited from required class)
     * and optional types(inherited from one of mentioned types).
     *
     * @param declaration The class declaration to check.
     * @param annotationType The annotation type that the class should be annotated with.
     * @param requiredType The required type that the class should inherit from.
     * @param optionals The optional types that the class can inherit from.
     */
    protected fun checkDeclarationHasType(
        declaration: KSClassDeclaration,
        annotationType: KClass<out Annotation>,
        requiredType: KClass<*>,
        vararg optionals: KClass<*>
    ) {
        check(declaration.containsSuperType(requiredType)) {
            logger.error(
                "Class ${
                    declaration.toClassName().reflectionName()
                } annotated with @${annotationType.simpleName} does not inherited from $requiredType"
            )
        }
        if (optionals.isNotEmpty()) {
            check(optionals.any { declaration.containsSuperType(it) }) {
                logger.error(
                    "Class ${declaration.toClassName().reflectionName()} annotated with " +
                            "@${annotationType.simpleName} has to be inherited from one" +
                            " of the type ${optionals.joinToString { it.qualifiedName.toString() }}"
                )
            }
        }
    }
}