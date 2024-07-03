package com.github.yuriisurzhykov.kevent.ksp.core

import com.github.yuriisurzhykov.kevent.events.codegen.Default
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Processes @Default annotation and parses it to [CodeBlock] with the provided default value.
 * */
interface DefaultAnnotationProcessor {

    fun buildDefault(property: PropertyKSClass, annotation: Default): CodeBlock

    /**
     * @property visitor The visitor interface to use to build the default value. When [buildDefault]
     * gets called this class walks through the possible property types and uses different methods
     * of this visitor to build a block of code with the default value. The default can be
     * represented as a runtime String, or as a string with code to execute to get a default value
     * @property useShortNames Flag that turns on and off the use of short names for primitive types.
     * Short names for primitive types are values without language literals (e.g. instead of 10L it
     * returns 10, or instead of "\"some string\"" it returns "some string")
     * */
    class Base(
        private val visitor: PrimitiveValueBuildVisitor = PrimitiveValueBuildVisitor.Base(),
        private val useShortNames: Boolean = false
    ) : DefaultAnnotationProcessor {

        override fun buildDefault(property: PropertyKSClass, annotation: Default): CodeBlock {
            return when {
                property.type.isPrimitive()           -> visitor.onPrimitive(
                    useShortNames,
                    property,
                    annotation
                )

                property.type.isEnum()                -> visitor.onEnum(
                    useShortNames,
                    property,
                    annotation
                )

                property.type.isEnumArray()           -> visitor.onEnumArray(
                    useShortNames,
                    property,
                    annotation
                )

                property.type.isEnumCollection()      -> visitor.onEnumCollection(
                    useShortNames,
                    property,
                    annotation
                )

                property.type.isPrimitiveArray()      -> visitor.onPrimitiveArray(
                    useShortNames,
                    property,
                    annotation
                )

                property.type.isPrimitiveCollection() -> visitor.onPrimitiveCollection(
                    useShortNames,
                    property,
                    annotation
                )

                else                                  -> throw IllegalAccessException(
                    "Unsupported type ${property.type.toTypeName()} to use with @Default annotation"
                )
            }
        }
    }
}