package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.property

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.globalLogger
import com.github.yuriisurzhykov.kevent.ksp.core.PrimitiveTypeValueBuilder
import com.github.yuriisurzhykov.kevent.ksp.core.PropertyKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.isEnum
import com.github.yuriisurzhykov.kevent.ksp.core.isEnumArray
import com.github.yuriisurzhykov.kevent.ksp.core.isEnumCollection
import com.github.yuriisurzhykov.kevent.ksp.core.isPrimitive
import com.github.yuriisurzhykov.kevent.ksp.core.isPrimitiveArray
import com.github.yuriisurzhykov.kevent.ksp.core.isPrimitiveCollection
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

fun PersistablePropertyKSClass(
    declaration: KSPropertyDeclaration,
    typeResolverStrategy: PersistablePropertyTypeStrategy = PersistablePropertyTypeStrategy.Base()
): PersistablePropertyKSClass {
    val isMutable = declaration.isMutable
    val declarationType = declaration.type.resolve()
    val propertyName = declaration.simpleName.asString()
    val className = typeResolverStrategy.resolveClassName(declarationType, globalLogger)

    return PersistablePropertyKSClass(
        isMutable,
        declarationType,
        propertyName,
        className,
        PropertyKSClass(declaration)
    )
}

fun PersistablePropertyKSClass(
    parameter: KSValueParameter,
    typeResolverStrategy: PersistablePropertyTypeStrategy = PersistablePropertyTypeStrategy.Base()
): PersistablePropertyKSClass {
    val isMutable = parameter.isVar
    val declarationType = parameter.type.resolve()
    val propertyName = parameter.name?.asString().toString()
    val className = typeResolverStrategy.resolveClassName(declarationType, globalLogger)

    return PersistablePropertyKSClass(
        isMutable,
        declarationType,
        propertyName,
        className,
        PropertyKSClass(parameter)
    )
}

fun PersistablePropertyKSClass(
    isMutable: Boolean,
    declarationType: KSType,
    propertyName: String,
    className: TypeName,
    property: PropertyKSClass
): PersistablePropertyKSClass {
    return when {
        declarationType.isEnum()                -> Enum(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )

        declarationType.isPrimitiveArray()      -> PrimitiveArray(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )

        declarationType.isEnumArray()           -> EnumArray(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )

        declarationType.isEnumCollection()      -> EnumCollection(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )

        declarationType.isPrimitiveCollection() -> PrimitiveCollection(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )

        else                                    -> Object(
            isMutable = isMutable,
            ksType = declarationType,
            propertyName = propertyName,
            sqlPropertyName = propertyName,
            className = className,
            property = property,
            logger = globalLogger
        )
    }
}

/**
 * Class representing wrapper for a property of the persistable event, that obtained either from
 * [KSPropertyDeclaration] or [KSValueParameter]. This class provides methods to generate
 * properties for the entity and code for how to use such properties in mappers. Because of
 * properties may be primitives, enums, or even collections, we need to handle all these cases
 * separately but the clients (classes that work with the properties) should not know about what
 * property they currently working with, they need only know the interface of property.
 */
interface PersistablePropertyKSClass {

    val sqlPropertyName: String
    val propertyName: String
    val className: TypeName

    fun asEntityConstructorParameter(): ParameterSpec
    fun asEntityProperty(): PropertySpec
    fun asMapperToEntityStatement(paramHolderName: String): CodeBlock
    fun asMapperFromEntityStatement(paramHolderName: String): CodeBlock
    fun propertyNameWithCast(name: String): CodeBlock
}

abstract class Abstract(
    private val isMutable: Boolean,
    protected val declarationType: KSType,
    protected val logger: KSPLogger? = null
) : PersistablePropertyKSClass {

    /**
     * Generates a [ParameterSpec] object representing a constructor parameter for an entity.
     *
     * @return a [ParameterSpec] object representing the constructor parameter
     */
    override fun asEntityConstructorParameter(): ParameterSpec {
        return ParameterSpec.builder(propertyName, className)
            .build()
    }

    override fun asEntityProperty(): PropertySpec {
        return PropertySpec.builder(propertyName, className)
            .mutable(isMutable)
            .initializer(propertyName)
            .build()
    }

    protected open fun columnInfoBuilder(): AnnotationSpec.Builder {
        return AnnotationSpec.builder(ColumnInfo::class)
            .addMember("name = \"%L\"", sqlPropertyName)
    }
}

abstract class Primitive(
    isMutable: Boolean,
    declarationType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    protected val property: PropertyKSClass,
    logger: KSPLogger? = null
) : Abstract(isMutable, declarationType, logger) {

    override fun columnInfoBuilder(): AnnotationSpec.Builder {
        val defaultValue = PrimitiveTypeValueBuilder(true)
            .build(property, null, false)

        return super.columnInfoBuilder()
            .addMember("defaultValue = %S", defaultValue)
    }

    override fun asEntityProperty(): PropertySpec {
        val columnInfoBuilder = columnInfoBuilder()

        return super.asEntityProperty().toBuilder()
            .addAnnotation(columnInfoBuilder.build())
            .build()
    }

    override fun asMapperToEntityStatement(paramHolderName: String): CodeBlock {
        val builder = CodeBlock.builder()
        if (paramHolderName.isNotEmpty()) {
            builder.add("%L.", paramHolderName)
        }
        builder.add("%L", propertyName)
        return builder.build()
    }

    override fun asMapperFromEntityStatement(paramHolderName: String): CodeBlock {
        val builder = CodeBlock.builder()
        if (paramHolderName.isNotEmpty()) {
            builder.add("%L.", paramHolderName)
        }
        builder.add("%L", propertyName)
        return builder.build()
    }

    override fun propertyNameWithCast(name: String): CodeBlock {
        return CodeBlock.builder()
            .add("%L as %T", name, className)
            .build()
    }
}

class Enum(
    isMutable: Boolean,
    ksType: KSType,
    property: PropertyKSClass,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    logger: KSPLogger? = null
) : Primitive(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun asMapperToEntityStatement(paramHolderName: String): CodeBlock {
        val builder = super.asMapperToEntityStatement(paramHolderName).toBuilder()
        if (className.isNullable) {
            builder.add("?")
        }
        builder.add(".toString()")
        return builder.build()
    }

    override fun asMapperFromEntityStatement(paramHolderName: String): CodeBlock {
        val builder = CodeBlock.builder()
        val paramDeclaration = "$paramHolderName.${propertyName}"
        if (className.isNullable) {
            builder.add("if ($paramDeclaration == null) null else ")
        }
        builder.add(
            "%T.valueOf(%L)",
            declarationType.toClassName(),
            paramDeclaration
        )
        return builder.build()
    }

    override fun propertyNameWithCast(name: String): CodeBlock {
        return CodeBlock.Builder()
            .add("%L.toString()", name)
            .build()
    }
}

abstract class Iterable(
    isMutable: Boolean,
    ksType: KSType,
    sqlPropertyName: String,
    propertyName: String,
    className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : Primitive(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    protected val genericType =
        ksType.arguments.firstOrNull()?.type?.resolve()?.declaration?.closestClassDeclaration()


    @Suppress("SameParameterValue")
    protected open fun toEntityElementMapper(mapName: String): CodeBlock {
        return CodeBlock.of("%L.toString()", mapName)
    }

    @Suppress("SameParameterValue")
    protected open fun fromEntityElementMapper(mapName: String): CodeBlock {
        return CodeBlock.of("%L.toString()", mapName)
    }

    protected open fun collectionFinalCast(): CodeBlock {
        return CodeBlock.of(".toTypedArray()")
    }

    override fun columnInfoBuilder(): AnnotationSpec.Builder {
        val builder = super.columnInfoBuilder()
        builder.members.removeAll { it.toString().startsWith("defaultValue") }
        val defaultAnnotation = property.defaultAnnotation
        if (defaultAnnotation != null) {
            val default = defaultAnnotation.value.replace(" ", "")
            builder.addMember("defaultValue = %S", default)
        } else if (className.isNullable) {
            builder.addMember("defaultValue = %S", "null")
        } else {
            builder.addMember("defaultValue = %S", "")
        }
        return builder
    }

    override fun asMapperToEntityStatement(paramHolderName: String): CodeBlock {
        val builder = CodeBlock.builder()
        if (paramHolderName.isNotEmpty()) {
            builder.add("%L.", paramHolderName)
        }
        builder.add("%L", propertyName)
        if (className.isNullable) builder.add("?")

        builder.add(".joinToString(\",\") { %L }", toEntityElementMapper("it"))
        return builder.build()
    }

    override fun asMapperFromEntityStatement(paramHolderName: String): CodeBlock {
        val builder = CodeBlock.builder()
        if (paramHolderName.isNotEmpty()) {
            builder.add("%L.", paramHolderName)
        }
        builder.add("%L", propertyName)

        if (className.isNullable) builder.add("?")

        builder.add(".split(\",\")")

        if (className.isNullable) builder.add("?")

        builder.add(".mapNotNull { %L }", fromEntityElementMapper("it"))

        if (className.isNullable) builder.add("?")
        builder.add(collectionFinalCast())
        return builder.build()
    }
}

open class PrimitiveArray(
    isMutable: Boolean,
    ksType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : Iterable(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun fromEntityElementMapper(mapName: String): CodeBlock {
        val mapElementType = declarationType.declaration.simpleName.asString().replace("Array", "")
        return CodeBlock.of("%L.to%L()", mapName, mapElementType)
    }

    override fun collectionFinalCast(): CodeBlock {
        val mapElementType = declarationType.declaration.simpleName.asString()
        return CodeBlock.of(".to%L()", mapElementType)
    }
}

open class EnumArray(
    isMutable: Boolean,
    ksType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : Iterable(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun fromEntityElementMapper(mapName: String): CodeBlock {
        return CodeBlock.of("try { %T.valueOf(%L) } catch (e: Exception) { null }", genericType?.toClassName(), mapName)
    }
}

class EnumCollection(
    isMutable: Boolean,
    ksType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : EnumArray(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun collectionFinalCast(): CodeBlock {
        val collectionType = declarationType.toClassName().simpleName
        return CodeBlock.of(".to%L()", collectionType)
    }
}

class PrimitiveCollection(
    isMutable: Boolean,
    ksType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : PrimitiveArray(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun fromEntityElementMapper(mapName: String): CodeBlock {
        val mapElementType = genericType?.simpleName?.asString()?.replace("Array", "")
        return CodeBlock.of("%L.to%L()", mapName, mapElementType)
    }

    override fun collectionFinalCast(): CodeBlock {
        val collectionType = declarationType.toClassName().simpleName
        return CodeBlock.of(".to%L()", collectionType)
    }
}

class Object(
    isMutable: Boolean,
    ksType: KSType,
    override val sqlPropertyName: String,
    override val propertyName: String,
    override val className: TypeName,
    property: PropertyKSClass,
    logger: KSPLogger? = null
) : Primitive(isMutable, ksType, sqlPropertyName, propertyName, className, property, logger) {

    override fun columnInfoBuilder(): AnnotationSpec.Builder {
        return if (property.type.isPrimitive()) {
            super.columnInfoBuilder()
        } else {
            AnnotationSpec.builder(Embedded::class)
        }
    }
}