package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.property.PersistablePropertyKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.containsSuperType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Represents a persistable entity(event) class declaration in a Kotlin Symbol Processor environment.
 *
 * @property declaration The KSClassDeclaration of the entity.
 * @property packageName The package name of the entity.
 * @property mapperActualClassName The actual class name of the entity mapper.
 * @property entityActualClassName The actual class name of the entity.
 * @property primaryKeysPoetFormat The primary keys in Poet format.
 * @property primaryKeyProperties The primary key properties of the entity.
 * @property tableName The table name of the entity.
 * @property entityProperties The properties of the entity.
 * @property primaryProperties The primary properties of the entity.
 * @property className The class name of the entity.
 * @property mapperClassName The class name of the entity mapper.
 * @property entityClassName The class name of the entity.
 */
class PersistableEntityKSClass(
    private val declaration: KSClassDeclaration
) {
    companion object {
        internal const val ENTITY_SUFFIX = "Entity"
        internal const val MAPPER_SUFFIX = "EntityMapper"
        internal const val DEFAULT_PRIMARY_KEY_NAME = "primaryKey"
        internal const val DEFAULT_PRIMARY_KEY_VALUE = "1"
    }

    val packageName = declaration.toClassName().packageName
    val mapperActualClassName = declaration.toClassName().simpleName.plus(MAPPER_SUFFIX)
    val entityActualClassName = declaration.toClassName().simpleName.plus(ENTITY_SUFFIX)
    val primaryKeysPoetFormat = declaration.primaryKeyPropertyNames().joinToString { "\"%L\"" }
    val primaryKeyProperties = declaration.primaryKeyPropertyNames()
    val tableName = declaration.simpleName.asString()

    val entityProperties =
        declaration.getAllProperties().map { PersistablePropertyKSClass(it) }

    val primaryProperties = if (declaration.primaryConstructor != null) {
        declaration.primaryConstructor!!.parameters.map {
            PersistablePropertyKSClass(it)
        }
    } else {
        declaration.getConstructors().first().parameters.map {
            PersistablePropertyKSClass(it)
        }
    }


    val className = declaration.toClassName()
    val mapperClassName = ClassName(packageName, mapperActualClassName)
    val entityClassName = ClassName(packageName, entityActualClassName)

    /**
     * Returns a list of [PropertySpec] objects representing the declared properties of an entity.
     *
     * @return a list of PropertySpec objects representing the declared properties
     */
    fun declaredProperties(): List<PropertySpec> {
        return entityProperties.map { it.asEntityProperty() }.toList()
    }

    /**
     * Returns a list of [ParameterSpec] objects representing the declared properties of an entity required for
     * constructor.
     *
     * @return a list of [ParameterSpec] objects representing the declared parameters
     */
    fun declaredParameters(): List<ParameterSpec> {
        return entityProperties.map { it.asEntityConstructorParameter() }.toList()
    }

    /**
     * Builds a default primary key parameter for the DB entity.
     *
     * @return The primary key parameter specification.
     */
    fun buildDefaultPrimaryKeyParameter(): ParameterSpec {
        return ParameterSpec.builder(
            DEFAULT_PRIMARY_KEY_NAME,
            Int::class.asTypeName()
        ).build()
    }

    /**
     * Builds a default primary key property for the DB entity.
     *
     * @return a [PropertySpec] representing the default primary key property
     */
    fun buildDefaultPrimaryKeyProperty(): PropertySpec {
        return PropertySpec.builder(
            DEFAULT_PRIMARY_KEY_NAME,
            Int::class.asTypeName()
        )
            .mutable(false)
            .addAnnotation(
                AnnotationSpec.builder(ColumnInfo::class)
                    .addMember("name = \"%L\"", DEFAULT_PRIMARY_KEY_NAME)
                    .addMember("defaultValue = \"%L\"", DEFAULT_PRIMARY_KEY_VALUE)
                    .build()
            )
            .initializer(DEFAULT_PRIMARY_KEY_NAME)
            .build()
    }

    /**
     * Checks if the class has a defined primary key. Returns true only if the class inherited either from
     * [Event.StickyCollection] or [Event.Sticky] interface and has at least one property to use as a primary key.
     *
     * @return true if the class has a defined primary key, false otherwise.
     */
    fun hasDefinedPrimaryKey(): Boolean {
        return declaration.containsSuperType(Event.StickyCollection::class)
    }

    /**
     * Retrieves the primary key property of the class.
     *
     * @return the primary key property, or null if no property is annotated with @PrimaryKey
     */
    @OptIn(KspExperimental::class)
    fun primaryKeyProperty(): Array<PersistablePropertyKSClass> {
        val tablePrimaryKeyProperty = declaration
            .getAllProperties()
            .firstOrNull { it.isAnnotationPresent(PrimaryKey::class) }

        return if (tablePrimaryKeyProperty != null) {
            arrayOf(PersistablePropertyKSClass(tablePrimaryKeyProperty))
        } else {
            declaration.primaryKeyProperties()
        }
    }

    /**
     * Resolves the table name for the given entity(based on event) class.
     *
     * @return the resolved table name
     */
    @OptIn(KspExperimental::class)
    fun resolveTableName(): String {
        val defaultTableName = tableName
        val declaredTableName =
            declaration.getAnnotationsByType(Entity::class).firstOrNull()?.tableName
        return if (declaredTableName.isNullOrEmpty()) defaultTableName
        else declaredTableName
    }

    private fun KSClassDeclaration.primaryKeyProperties(): Array<PersistablePropertyKSClass> {
        val primaryKeys = mutableListOf<PersistablePropertyKSClass>()
        if (containsSuperType(Event.KeyValidatable::class)) {
            primaryKeys.addAll(
                entityProperties
                    .filter { it.propertyName == Event.KeyValidatable<*>::key.name }
                    .toList()
            )
        }
        return primaryKeys.toTypedArray()
    }

    /**
     * Returns an array of property names that make up the primary key for the given [KSClassDeclaration].
     * If the class contains only [Event.StickyCollection], the array will contain the name of the
     * property [Event.StickyCollection.key]. If the class does not contain any of the super types,
     * the array will contain the default primary key name [DEFAULT_PRIMARY_KEY_NAME].
     *
     * @return an array of property names that make up the primary key
     */
    private fun KSClassDeclaration.primaryKeyPropertyNames(): Array<String> {
        return if (containsSuperType(Event.StickyCollection::class)) {
            arrayOf(Event.StickyCollection<*>::key.name)
        } else {
            arrayOf(DEFAULT_PRIMARY_KEY_NAME)
        }
    }
}