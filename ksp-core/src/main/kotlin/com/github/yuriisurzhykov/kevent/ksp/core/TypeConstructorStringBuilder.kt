package com.github.yuriisurzhykov.kevent.ksp.core

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.github.yuriisurzhykov.kevent.events.codegen.IllegalDefaultFormatException
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Interface defining a mechanism for building default value strings for various data types.
 * Used in the generation of code for default parameter values in constructors.
 */
interface TypeConstructorStringBuilder {

    /**
     * Builds a default value string for a given type.
     *
     * @param valueParam The declaration representing the type.
     * @return A string representation of the default value for the specified type.
     */
    fun build(
        valueParam: PropertyKSClass,
        defaultString: String? = null,
        trimValues: Boolean
    ): CodeBlock

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Boolean values.
     */
    class BooleanBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Boolean") {
                try {
                    val initString = defaultString?.toBooleanStrict()?.toString() ?: "false"
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Boolean!")
                }
            } else throw IllegalArgumentException("Class is not Boolean")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Byte values.
     */
    class ByteBuilder(
        private val isUnsigned: Boolean = false
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Byte" || className == "kotlin.UByte") {
                try {
                    val initString = if (isUnsigned) {
                        defaultString?.toUByte()?.toString() ?: "0u".trimLiterals(!trimValues)
                    } else {
                        defaultString?.toByte()?.toString() ?: "0"
                    }
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Byte!")
                }
            } else throw IllegalArgumentException("Class is not Byte")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Char values.
     */
    class CharBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Char") {
                try {
                    val initString = defaultString?.get(0)?.toString() ?: "'0'"
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Char!")
                }
            } else throw IllegalArgumentException("Class is not Byte")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Integer values.
     */
    class IntBuilder(
        private val isUnsigned: Boolean = false
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Int" || className == "kotlin.UInt") {
                try {
                    val initString = if (isUnsigned) {
                        defaultString?.toUInt()?.toString() ?: "0u".trimLiterals(trimValues)
                    } else {
                        defaultString?.toInt()?.toString() ?: "0"
                    }
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not an Int!")
                }
            } else throw IllegalArgumentException("Class is not Integer")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Float values.
     */
    class FloatBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Float") {
                try {
                    val initString = defaultString?.toFloat()?.toString() ?: "0.0f".trimLiterals(
                        trimValues
                    )
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Float!")
                }
            } else throw IllegalArgumentException("Class is not Float")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Double values.
     */
    class DoubleBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Double") {
                try {
                    val initString = defaultString?.toDouble()?.toString() ?: "0.0"
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Double!")
                }
            } else throw IllegalArgumentException("Class is not Double")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Long values.
     */
    class LongBuilder(
        private val isUnsigned: Boolean = false
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Long" || className == "kotlin.ULong") {
                try {
                    val initString = if (isUnsigned) {
                        defaultString?.toULong()?.toString() ?: "0uL".trimLiterals(
                            trimValues
                        )
                    } else {
                        defaultString?.toLong()?.toString() ?: "0L".trimLiterals(
                            trimValues
                        )
                    }
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Long!")
                }
            } else throw IllegalArgumentException("Class is not a Long")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default Short values.
     */
    class ShortBuilder(
        private val isUnsigned: Boolean = false
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.Short" || className == "kotlin.UShort") {
                try {
                    val initString = if (isUnsigned) {
                        defaultString?.toUShort()?.toString() ?: "0u".trimLiterals(
                            trimValues
                        )
                    } else {
                        defaultString?.toShort()?.toString() ?: "0"
                    }
                    CodeBlock.of(initString)
                } catch (e: Exception) {
                    throw IllegalDefaultFormatException("The default value $defaultString is not a Short!")
                }
            } else throw IllegalArgumentException("Class is not a Short")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default String values.
     */
    class StringBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className == "kotlin.String" || className == "java.lang.String") {
                if (defaultString.isNullOrEmpty()) CodeBlock.of(
                    "\"\"".trimLiterals(trimValues)
                )
                else CodeBlock.of(
                    "\"%L\"".trimLiterals(trimValues), defaultString
                )
            } else throw IllegalArgumentException("Class is not String")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default any of array values.
     */
    class ArrayBuilder(
        private val prefix: String? = null
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className in MapOfArrays.keys) {
                CodeBlock.of(if (prefix == null) "arrayOf()" else "${prefix}ArrayOf()")
            } else throw IllegalArgumentException("Class is not an Array")
        }
    }

    /**
     * Implementation of [TypeConstructorStringBuilder] for generating default collection values.
     */
    class CollectionBuilder(
        private val invocationStatement: String? = null
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val className = valueParam.typeStringName
            return if (className in MapOfCollections.keys) {
                CodeBlock.of(invocationStatement ?: "$className()")
            } else throw IllegalArgumentException("Class is not a Map")
        }
    }

    /**
     *  Implementation of [TypeConstructorStringBuilder] for generating custom enum types values.
     *  By default it returns first value of enum list.
     */
    class EnumBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {

            if (valueParam.classKind == ClassKind.ENUM_CLASS) {
                // Obtaining all enum values
                val enumValues = valueParam.declaration.enumEntriesNames()

                // Return first value from enum list or null if list is empty
                return if (enumValues.firstOrNull() != null) {
                    if (trimValues) {
                        CodeBlock.of("%L", enumValues.first())
                    } else {
                        CodeBlock.of("%T.%L", valueParam.typeName, enumValues.first())
                    }
                } else if (valueParam.isNullable) {
                    CodeBlock.of("null")
                } else throw IllegalArgumentException(
                    "Not able to provide default value for class: ${valueParam.typeStringName}"
                )
            } else {
                throw IllegalArgumentException("Class is not an Enum")
            }
        }
    }

    /**
     *  Implementation of [TypeConstructorStringBuilder] for generating custom enum types values.
     *  By default it returns first value of enum list.
     */
    class ObjectTypeBuilder : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            if (valueParam.classKind == ClassKind.OBJECT) {
                // Return first value from enum list or null if list is empty
                return if (valueParam.isNullable) CodeBlock.of("null")
                else CodeBlock.of("%T", valueParam.typeName)
            } else {
                throw IllegalArgumentException("Class is not an object!")
            }
        }
    }

    class KSTypeValueBuilder(
        private val shortNames: Boolean
    ) : TypeConstructorStringBuilder {

        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            val typeName = valueParam.typeStringName
            return when {
                valueParam.defaultAnnotation != null -> {
                    DefaultAnnotationProcessor.Base(useShortNames = shortNames)
                        .buildDefault(valueParam, valueParam.defaultAnnotation)
                }

                typeName in MapOfBasicTypes.keys || valueParam.type.isEnum() -> {
                    PrimitiveTypeValueBuilder(shortNames).build(
                        valueParam,
                        defaultString,
                        trimValues
                    )
                }

                typeName in MapOfCollections.keys -> {
                    MapOfCollections[typeName]!!.build(valueParam, trimValues = true)
                }

                else -> {
                    val codeBlock = when (valueParam.classKind) {
                        ClassKind.OBJECT -> ObjectTypeBuilder()
                        else             -> CustomTypeBuilder()
                    }
                    codeBlock.build(valueParam, trimValues = true)
                }
            }
        }
    }

    /**
     * Implementation for building default values for custom types.
     * It analyzes constructors and generates default parameter values accordingly.
     *
     * @param logger The KSPLogger for logging information.
     */
    class CustomTypeBuilder(
        private val logger: KSPLogger? = null
    ) : TypeConstructorStringBuilder {
        override fun build(
            valueParam: PropertyKSClass,
            defaultString: String?,
            trimValues: Boolean
        ): CodeBlock {
            // Try to find class declaration for the given symbol
            val declaration = valueParam.declaration.closestClassDeclaration()
            return if (declaration != null) {
                buildConstructorDeclaration(declaration)
            } else if (valueParam.isNullable) {
                // If no class definition found and parameter marked as null then passing null
                logger?.warn("No declaration found for ${valueParam.typeStringName}. Using 'null' for it")
                CodeBlock.of("null")
            } else {
                // If no definition and param not nullable throwing error
                throw IllegalStateException(
                    "Not possible to create default parameters for class ${valueParam.typeStringName}"
                )
            }
        }

        private fun buildConstructorDeclaration(declaration: KSClassDeclaration): CodeBlock {
            // Define string builder for constructor invocation string
            val constructorStringBuilder = CodeBlock.builder()
            constructorStringBuilder.add("%T", declaration.toClassName()).add("(")

            // Try to find constructor without parameters
            val emptyConstructor =
                declaration.getConstructors().find { it.parameters.isEmpty() }

            if (emptyConstructor == null) {
                logger?.info("No empty constructor for ${declaration.qualifiedName?.asString()}")

                // Try to find constructor without custom data types.
                val primaryConstructor = declaration.getConstructors().firstOrNull()
                if (primaryConstructor != null) {
                    // Receive all parameter for primary constructor
                    val params = primaryConstructor.parameters

                    logger?.info("Using primary constructor: ${primaryConstructor.qualifiedName?.asString()}")

                    // Mapping every data type to its corresponding allocation string
                    params.forEachIndexed { index, param: KSValueParameter ->
                        val propertyClass = PropertyKSClass(param)

                        // Skip initialize parameter only if no @Default annotation for it but the
                        // runtime default value provided so we can not worry about what value to
                        // provide.
                        if (propertyClass.defaultAnnotation == null && param.hasDefault) return@forEachIndexed

                        val paramName = param.name?.asString().toString()
                        constructorStringBuilder.add("%L = ", paramName)
                        constructorStringBuilder.add(
                            KSTypeValueBuilder(false).build(
                                propertyClass,
                                trimValues = true
                            )
                        )

                        if (index < params.size - 1) {
                            constructorStringBuilder.add(", ")
                        }
                    }
                }
            }
            // Closing constructor parentheses and returning constructor invocation string
            return constructorStringBuilder.add(")").build()
        }
    }
}