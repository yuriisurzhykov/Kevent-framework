@file:Suppress("unused")

package com.github.yuriisurzhykov.kevent.ksp.core

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.tags.TypeAliasTag
import kotlin.reflect.KClass

/*
 * Constants to use for generators
 */

const val IMPL_NAME = "Impl"
private val arrayTypes = setOf(
    "kotlin.ByteArray", "kotlin.ShortArray", "kotlin.IntArray", "kotlin.LongArray",
    "kotlin.FloatArray", "kotlin.DoubleArray",
    "kotlin.BooleanArray",
    "kotlin.CharArray"
)

private val primitiveTypes = setOf(
    "kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
    "kotlin.Float", "kotlin.Double",
    "kotlin.UByte", "kotlin.UShort", "kotlin.UInt", "kotlin.ULong",
    "kotlin.Boolean",
    "kotlin.Char",
    "kotlin.String",
    "java.lang.String"
)

/*
 * Provides helper functions and collections for handling symbol processing with Kotlin KSP.
 */

/**
 * Retrieves symbols(classes) annotated with a given annotation.
 *
 * @param cls The KClass of the annotation to search for.
 * @return A sequence of KSClassDeclarations annotated with the specified annotation.
 */
fun Resolver.getSymbols(
    cls: KClass<*>
) = this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
    .filterIsInstance<KSClassDeclaration>()

/**
 * Checks if the collection of modifiers contains a specific name.
 *
 * @param name The name of the modifier to check for.
 * @return True if any modifier in the collection contains the specified name.
 */
fun Collection<Modifier>.contains(name: String): Boolean {
    return any { it.name.contains(name) }
}

fun <T : Any> KSClassDeclaration.containsSuperType(clazz: KClass<T>): Boolean {
    return getAllSuperTypes()
        .any { type -> type.declaration.qualifiedName?.asString() == clazz.qualifiedName }
}

fun KSType.isArray(): Boolean {
    return declaration.qualifiedName?.asString() == "kotlin.Array"
}

fun KSType.isString(): Boolean {
    val stringTypes = setOf(
        "kotlin.String", "java.lang.String"
    )
    return declaration.qualifiedName?.asString() in stringTypes
}

fun TypeName.isPrimitive(): Boolean {
    val typeName = toString()
    return typeName in primitiveTypes
}

fun KSType.isPrimitive(): Boolean {
    var typeName: TypeName = toClassName()
    val typeAlias = typeName.tag(TypeAliasTag::class)?.abbreviatedType
    if (typeAlias != null) {
        typeName = typeAlias
    }
    return typeName.isPrimitive()
}

fun TypeName.isPrimitiveArray(): Boolean {
    return this.toString() in arrayTypes
}

fun KSType.isPrimitiveArray(): Boolean {
    var typeName: TypeName = toClassName()
    val typeAlias = typeName.tag(TypeAliasTag::class)?.abbreviatedType
    if (typeAlias != null) {
        typeName = typeAlias
    }
    return typeName.isPrimitiveArray()
}

fun KSType.isEnumArray(): Boolean {
    return isCollectionGeneric(
        validate = { it.classKind == ClassKind.ENUM_CLASS },
        collectionNames = arrayOf("kotlin.Array")
    )
}

fun KSType.isEnumCollection(): Boolean {
    return isCollectionGeneric(
        validate = { it.classKind == ClassKind.ENUM_CLASS },
        collectionNames = (SetClassNames + ListClassNames + DequeClassNames).keys.toTypedArray()
    )
}

fun KSType.isPrimitiveCollection(): Boolean {
    return isCollectionGeneric(
        validate = { it.toClassName().isPrimitive() },
        collectionNames = (SetClassNames + ListClassNames + DequeClassNames).keys.toTypedArray()
    )
}

fun KSType.isCollectionGeneric(
    validate: (KSClassDeclaration) -> Boolean,
    vararg collectionNames: String
): Boolean {
    val typeParameter = arguments.firstOrNull() ?: return false
    val declarationName = declaration.qualifiedName?.asString()
    val closestDeclaration =
        typeParameter.type?.resolve()?.declaration?.closestClassDeclaration() ?: return false
    val genericIsValidated = validate.invoke(closestDeclaration)
    return declarationName in collectionNames && genericIsValidated
}

fun KSType.isEnum(): Boolean {
    return (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
}

fun KSDeclaration.enumEntriesNames(): List<String> {
    return if (this is KSClassDeclaration) {
        declarations.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .map { it.simpleName.asString() }.toList()
    } else emptyList()
}

fun KSType.genericType(): KSClassDeclaration? {
    val typeParameter = arguments.firstOrNull()
    return typeParameter?.type?.resolve()?.declaration?.closestClassDeclaration()
}

fun KSType.genericEnumEntriesNames(): List<String> {
    val genericType = genericType() ?: return emptyList()
    return genericType.declarations.filterIsInstance<KSClassDeclaration>()
        .filter { it.classKind == ClassKind.ENUM_ENTRY }
        .map { it.simpleName.asString() }.toList()
}

/*
 * Maps of various collection class names to their corresponding string builders.
 */
internal val ListClassNames = mapOf(
    "kotlin.collections.MutableList" to TypeConstructorStringBuilder.CollectionBuilder("mutableListOf()"),
    "kotlin.collections.ArrayList" to TypeConstructorStringBuilder.CollectionBuilder(),
    "kotlin.collections.List" to TypeConstructorStringBuilder.CollectionBuilder("listOf()"),
    "java.util.List" to TypeConstructorStringBuilder.CollectionBuilder("listOf()"),
    "java.util.ArrayList" to TypeConstructorStringBuilder.CollectionBuilder(),
)

internal val SetClassNames = mapOf(
    "kotlin.collections.MutableSet" to TypeConstructorStringBuilder.CollectionBuilder("mutableSetOf()"),
    "kotlin.collections.Set" to TypeConstructorStringBuilder.CollectionBuilder("setOf()"),
    "java.util.Set" to TypeConstructorStringBuilder.CollectionBuilder("setOf()"),
    "kotlin.collections.HashSet" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.HashSet" to TypeConstructorStringBuilder.CollectionBuilder(),
    "kotlin.collections.LinkedHashSet" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.LinkedHashSet" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.NavigableSet" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.SortedSet" to TypeConstructorStringBuilder.CollectionBuilder()
)

internal val DequeClassNames = mapOf(
    "kotlin.collections.ArrayDeque" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.ArrayDeque" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.Deque" to TypeConstructorStringBuilder.CollectionBuilder("java.util.ArrayDeque()")
)

internal val MapClassNames = mapOf(
    "kotlin.collections.MutableMap" to TypeConstructorStringBuilder.CollectionBuilder("mutableMapOf()"),
    "kotlin.collections.Map" to TypeConstructorStringBuilder.CollectionBuilder("mapOf()"),
    "java.util.Map" to TypeConstructorStringBuilder.CollectionBuilder("mutableMapOf()"),
    "kotlin.collections.HashMap" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.HashMap" to TypeConstructorStringBuilder.CollectionBuilder(),
    "kotlin.collections.LinkedHashMap" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.LinkedHashMap" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.SortedMap" to TypeConstructorStringBuilder.CollectionBuilder("java.util.TreeMap()"),
    "java.util.NavigableMap" to TypeConstructorStringBuilder.CollectionBuilder("java.util.TreeMap()"),
    "java.util.TreeMap" to TypeConstructorStringBuilder.CollectionBuilder(),
    "java.util.concurrent.ConcurrentMap" to TypeConstructorStringBuilder.CollectionBuilder("java.util.concurrent.ConcurrentHashMap()"),
    "java.util.concurrent.ConcurrentHashMap" to TypeConstructorStringBuilder.CollectionBuilder()
)

internal val MapOfCollections =
    (MapClassNames + SetClassNames + ListClassNames + DequeClassNames)

internal val MapOfUnsignedTypes = mapOf(
    "kotlin.UByte" to TypeConstructorStringBuilder.ByteBuilder(true),
    "kotlin.UShort" to TypeConstructorStringBuilder.ShortBuilder(true),
    "kotlin.UInt" to TypeConstructorStringBuilder.IntBuilder(true),
    "kotlin.ULong" to TypeConstructorStringBuilder.LongBuilder(true)
)

internal val MapOfArrays = mapOf(
    "kotlin.Array" to TypeConstructorStringBuilder.ArrayBuilder(),
    "kotlin.BooleanArray" to TypeConstructorStringBuilder.ArrayBuilder("boolean"),
    "kotlin.ByteArray" to TypeConstructorStringBuilder.ArrayBuilder("byte"),
    "kotlin.CharArray" to TypeConstructorStringBuilder.ArrayBuilder("char"),
    "kotlin.DoubleArray" to TypeConstructorStringBuilder.ArrayBuilder("double"),
    "kotlin.FloatArray" to TypeConstructorStringBuilder.ArrayBuilder("float"),
    "kotlin.IntArray" to TypeConstructorStringBuilder.ArrayBuilder("int"),
    "kotlin.LongArray" to TypeConstructorStringBuilder.ArrayBuilder("long"),
    "kotlin.ShortArray" to TypeConstructorStringBuilder.ArrayBuilder("short"),
)

internal val MapOfBasicTypes = mapOf(
    "kotlin.Boolean" to TypeConstructorStringBuilder.BooleanBuilder(),
    "kotlin.Byte" to TypeConstructorStringBuilder.ByteBuilder(),
    "kotlin.Short" to TypeConstructorStringBuilder.ShortBuilder(),
    "kotlin.Int" to TypeConstructorStringBuilder.IntBuilder(),
    "kotlin.Long" to TypeConstructorStringBuilder.LongBuilder(),
    "kotlin.Float" to TypeConstructorStringBuilder.FloatBuilder(),
    "kotlin.Double" to TypeConstructorStringBuilder.DoubleBuilder(),
    "kotlin.Char" to TypeConstructorStringBuilder.CharBuilder(),
    "kotlin.String" to TypeConstructorStringBuilder.StringBuilder(),
    "java.lang.String" to TypeConstructorStringBuilder.StringBuilder(),
) + MapOfUnsignedTypes + MapOfArrays