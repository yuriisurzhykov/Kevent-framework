package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.property

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.property.PersistablePropertyTypeStrategy.Base
import com.github.yuriisurzhykov.kevent.ksp.core.isEnum
import com.github.yuriisurzhykov.kevent.ksp.core.isEnumArray
import com.github.yuriisurzhykov.kevent.ksp.core.isEnumCollection
import com.github.yuriisurzhykov.kevent.ksp.core.isPrimitiveArray
import com.github.yuriisurzhykov.kevent.ksp.core.isPrimitiveCollection
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.tags.TypeAliasTag

/**
 * This interface  provides a strategy for resolving the property type for persistable events,
 * allowing for different handling of different property types. This interface has 4 base
 * implementations for different data types. In most cases the [TypeName] of the class is used
 * for property type, but for primitive arrays, enums and collections of enums the [String] is used
 * instead.
 *
 * The [Base] class is a delegate that knows what strategy to use based on some conditions.
 * */
interface PersistablePropertyTypeStrategy {

    fun resolveClassName(type: KSType, logger: KSPLogger?): TypeName

    /**
     * Abstract class that always returns [String] as the property type.
     * It allows properties to be nullable if any defined as nullable in events class.
     * */
    private abstract class AbstractString : PersistablePropertyTypeStrategy {
        override fun resolveClassName(type: KSType, logger: KSPLogger?): TypeName =
            String::class.asClassName().copy(nullable = type.isMarkedNullable)
    }

    private class Enum : AbstractString()

    private class PrimitiveArray : AbstractString()

    private class PrimitiveCollection : AbstractString()

    private class EnumArray : AbstractString()

    private class Other : PersistablePropertyTypeStrategy {
        override fun resolveClassName(type: KSType, logger: KSPLogger?): TypeName {
            // Use declaration class, but firstly check if this class is a 'typealias'.
            // If so, then we need to find actual implementation and use it for the type or
            // the declaration type of the property.
            val typeName = type.toTypeName()
            val typealiasTag = typeName.tag(TypeAliasTag::class)
            val returnTypeName = typealiasTag?.abbreviatedType ?: typeName

            return returnTypeName.copy(nullable = type.isMarkedNullable)
        }
    }

    class Base : PersistablePropertyTypeStrategy {
        override fun resolveClassName(type: KSType, logger: KSPLogger?): TypeName {
            return when {
                type.isEnum()                -> Enum().resolveClassName(type, logger)
                type.isEnumArray()           -> EnumArray().resolveClassName(type, logger)
                type.isPrimitiveArray()      -> PrimitiveArray().resolveClassName(type, logger)
                type.isEnumCollection()      -> PrimitiveCollection().resolveClassName(type, logger)
                type.isPrimitiveCollection() -> PrimitiveCollection().resolveClassName(type, logger)
                else                         -> Other().resolveClassName(type, logger)
            }
        }
    }
}