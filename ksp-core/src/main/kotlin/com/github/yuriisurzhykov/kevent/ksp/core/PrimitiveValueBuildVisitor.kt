package com.github.yuriisurzhykov.kevent.ksp.core

import com.github.yuriisurzhykov.kevent.events.codegen.Default
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * The visitor interface represents a visitor patter to make the work with the default primitive
 * types easier. For each primitive group of types there is a separate visitor method that separate
 * responsibility for how to build the default value.
 * */
interface PrimitiveValueBuildVisitor {

    fun onString(
        useShortNames: Boolean,
        propertyKSClass: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onEnum(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onPrimitive(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onEnumArray(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onEnumCollection(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onPrimitiveArray(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    fun onPrimitiveCollection(
        useShortNames: Boolean,
        property: PropertyKSClass,
        annotation: Default
    ): CodeBlock

    class Base : PrimitiveValueBuildVisitor {
        override fun onString(
            useShortNames: Boolean,
            propertyKSClass: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            return if (useShortNames) {
                CodeBlock.of("%L", annotation.value.trim())
            } else {
                CodeBlock.of("\"%L\"", annotation.value.trim())
            }
        }

        override fun onEnum(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            val enumNames = property.type.declaration.enumEntriesNames()
            if (annotation.value in enumNames) {
                val builder = CodeBlock.builder()
                // For some cases we need to provide only enum names instead of full type with
                // imports. When useShortName is true we use only entries names. Otherwise we use
                // full type with imports
                if (useShortNames) {
                    builder.add("%L", annotation.value)
                } else {
                    builder.add("%T.%L", property.type.toClassName(), annotation.value)
                }
                return builder.build()
            } else if (property.isNullable && annotation.value == "null") {
                return CodeBlock.of("null")
            } else {
                throw IllegalArgumentException("Provided default value ${annotation.value} doesn't match any of enum names: $enumNames")
            }
        }

        override fun onPrimitive(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            // For primitives we need to provide only primitive value either with language literals
            // or without. When useShortName is true the only values without literals are used:
            // e.g. 10 instead of 10L or 10.3 instead of 10.3f.
            return CodeBlock.of(
                "%L",
                MapOfBasicTypes[property.typeStringName]?.build(
                    property,
                    annotation.value,
                    useShortNames
                )
            )
        }

        override fun onEnumArray(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            val genericType = property.type.genericType()?.toClassName()
            val enumNames = property.type.genericEnumEntriesNames()
            val requestedValues = splitAnnotationValues(annotation)
            val allValuesCorrect = requestedValues.all { it in enumNames }
            if (allValuesCorrect) {
                val builder = arrayOfBuilder()
                requestedValues.forEachIndexed { index, value ->
                    if (useShortNames) {
                        builder.add("%L", value)
                    } else {
                        builder.add("%T.%L", genericType, value)
                    }
                    if (index != requestedValues.lastIndex) {
                        builder.add(", ")
                    }
                }
                return builder.add(")").build()
            } else if (property.isNullable && annotation.value == "null") {
                return CodeBlock.of("null")
            } else {
                throw IllegalArgumentException("${requestedValues.all { it in enumNames }} $requestedValues Provided default value ${annotation.value} doesn't match any of enum names: $enumNames")
            }
        }

        override fun onEnumCollection(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            val propertyType = property.type
            val genericType = propertyType.genericType()?.toClassName()
            val enumNames = propertyType.genericEnumEntriesNames()
            val requestedValues = splitAnnotationValues(annotation)

            val allValuesCorrect = requestedValues.all { it in enumNames }
            if (allValuesCorrect) {
                val builder = collectionOfBuilder(propertyType.toClassName())
                requestedValues.forEachIndexed { index, value ->
                    if (useShortNames) {
                        builder.add("%L", value)
                    } else {
                        builder.add("%T.%L", genericType, value)
                    }
                    if (index != requestedValues.lastIndex) {
                        builder.add(", ")
                    }
                }
                return builder.add(")").build()
            } else if (property.isNullable && annotation.value == "null") {
                return CodeBlock.of("null")
            } else {
                throw IllegalArgumentException("${requestedValues.all { it in enumNames }} $requestedValues Provided default value ${annotation.value} doesn't match any of enum names: $enumNames")
            }
        }

        override fun onPrimitiveArray(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            val propertyType = property.type
            val genericType = propertyType.genericType()?.toClassName()
            val enumNames = propertyType.genericEnumEntriesNames()
            val requestedValues = splitAnnotationValues(annotation)

            val allValuesCorrect = requestedValues.all { it in enumNames }
            if (allValuesCorrect) {
                val builder = arrayOfBuilder()
                requestedValues.forEachIndexed { index, value ->
                    val default =
                        MapOfBasicTypes[genericType.toString()]?.build(
                            property,
                            value,
                            useShortNames
                        )
                    if (default != null) {
                        builder.add(default)
                        if (index != requestedValues.lastIndex) {
                            builder.add(", ")
                        }
                    }
                }
                return builder.add(")").build()
            } else if (property.isNullable && annotation.value == "null") {
                return CodeBlock.of("null")
            } else {
                throw IllegalArgumentException("${requestedValues.all { it in enumNames }} $requestedValues Provided default value ${annotation.value} doesn't match any of enum names: $enumNames")
            }
        }

        override fun onPrimitiveCollection(
            useShortNames: Boolean,
            property: PropertyKSClass,
            annotation: Default
        ): CodeBlock {
            val propertyType = property.type
            val genericType = propertyType.genericType()?.toClassName()
            val enumNames = propertyType.genericEnumEntriesNames()
            val requestedValues = splitAnnotationValues(annotation)

            val allValuesCorrect = requestedValues.all { it in enumNames }
            if (allValuesCorrect) {
                val builder = collectionOfBuilder(propertyType.toClassName())
                requestedValues.forEachIndexed { index, value ->
                    val default =
                        MapOfBasicTypes[genericType.toString()]?.build(
                            property,
                            value,
                            useShortNames
                        )
                    if (default != null) {
                        builder.add(default)
                        if (index != requestedValues.lastIndex) {
                            builder.add(", ")
                        }
                    }
                }
                return builder.add(")").build()
            } else if (property.isNullable && annotation.value == "null") {
                return CodeBlock.of("null")
            } else {
                throw IllegalArgumentException("${requestedValues.all { it in enumNames }} $requestedValues Provided default value ${annotation.value} doesn't match any of enum names: $enumNames")
            }
        }

        private fun arrayOfBuilder() = CodeBlock.of("arrayOf").toBuilder()

        private fun collectionOfBuilder(className: ClassName) = CodeBlock.builder()
            .add("%LOf", className.simpleName.replaceFirstChar { it.lowercase() })
            .add("(")

        private fun splitAnnotationValues(annotation: Default): List<String> {
            // Using replace, because Kotlin's trim() method doesn't work
            return annotation.value.replace(" ", "").split(",")
        }
    }
}