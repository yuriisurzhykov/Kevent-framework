package com.github.yuriisurzhykov.kevent.ksp.core

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.github.yuriisurzhykov.kevent.events.codegen.Default
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.tags.TypeAliasTag

/**
 * This class is a wrapper around Kotlin Symbol Processor (KSP) property types.
 * It provides several utilities to fetch and process properties details from KSP types.
 *
 * It wraps around KSP's [KSValueParameter] and [KSPropertyDeclaration].
 *
 * @property hasDefault Indicates if the property has defined default value.
 * @property isNullable Indicates if the property is marked as nullable.
 * @property typeStringName The string name of the type of the property with package name.
 * @property typeName The actual type name of the property to work with Kotlin Poet library
 * @property defaultAnnotation The @Default annotation if present.
 * @property classKind Kind of class in the property (if any), for example object class, or enum class.
 * @property declaration The KSP declaration of the property.
 * @property type The KSP Type of the property.
 * @property typeAliasTag Provides information if current type is alias.
 */
class PropertyKSClass {

    val hasDefault: Boolean
    val isNullable: Boolean
    val typeStringName: String
    val typeName: TypeName
    val defaultAnnotation: Default?
    val classKind: ClassKind?
    val declaration: KSDeclaration
    val type: KSType
    private val typeAliasTag: TypeName?

    /**
     * Primary constructor, initialises PropertyKSClass with [KSValueParameter].
     *
     * @param value KSValueParameter for the intended property.
     */
    @OptIn(KspExperimental::class)
    constructor(value: KSValueParameter) {
        // Resolving property type to minimize resolving calls to save the build time
        // because it's heavy operation.
        val valueType = value.type.resolve()
        type = valueType
        val tempTypeName = valueType.toTypeName()
        // Check if property class is a typealias
        typeAliasTag = tempTypeName.tag(TypeAliasTag::class)?.abbreviatedType
        typeName = typeAliasTag ?: valueType.toTypeName()
        hasDefault = value.hasDefault
        isNullable = typeAliasTag?.isNullable ?: valueType.isMarkedNullable

        // Using typeAlias name or declaration name as type string name
        // because reading typeName.toString() returns generic types in format '<another type>'
        // instead of just base type. For example: instead of `kotlin.collections.List` it
        // returns kotlin.collections.List<kotlin.Int>
        typeStringName =
            typeAliasTag?.toString() ?: valueType.declaration.qualifiedName?.asString().toString()
        defaultAnnotation = value.getAnnotationsByType(Default::class).firstOrNull()
        classKind = (valueType.declaration as? KSClassDeclaration)?.classKind
        declaration = valueType.declaration
    }

    /**
     * Secondary constructor, initialises PropertyKSClass with [KSPropertyDeclaration].
     *
     * @param property KSPropertyDeclaration for the intended property.
     */
    @OptIn(KspExperimental::class)
    constructor(property: KSPropertyDeclaration) {
        // Resolving property type to minimize resolving calls to save the build time
        // because it's heavy operation.
        val valueType = property.type.resolve()
        val tempTypeName = valueType.toTypeName()
        type = valueType
        hasDefault = false
        // Check if property class is a typealias
        typeAliasTag = tempTypeName.tag(TypeAliasTag::class)?.abbreviatedType
        typeName = typeAliasTag ?: valueType.toTypeName()
        isNullable = typeAliasTag?.isNullable ?: valueType.isMarkedNullable
        typeStringName =
            typeAliasTag?.toString() ?: valueType.declaration.qualifiedName?.asString().toString()
        defaultAnnotation = property.getAnnotationsByType(Default::class).firstOrNull()
        classKind = (valueType.declaration as? KSClassDeclaration)?.classKind
        declaration = valueType.declaration
    }

    /**
     * Tertiary constructor, initialises PropertyKSClass with [KSClassDeclaration].
     *
     * @param clazz KSClassDeclaration for the intended property.
     */
    constructor(clazz: KSClassDeclaration) {
        type = clazz.asType(emptyList())
        hasDefault = false
        isNullable = false
        typeStringName = clazz.qualifiedName?.asString().toString()
        typeName = clazz.toClassName()
        typeAliasTag = null
        defaultAnnotation = null
        classKind = clazz.classKind
        declaration = clazz
    }
}