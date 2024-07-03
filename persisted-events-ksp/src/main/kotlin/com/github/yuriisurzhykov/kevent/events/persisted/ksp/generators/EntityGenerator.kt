package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators

import androidx.room.Entity
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.AutoEntity
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableEntityKSClass
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * `EntityGenerator` class is used to generate entities for events defined as @PersistableEvent.
 * It is responsible for generating database entities annotated with Room's Entity annotation.
 *
 * Example of output:
 * Let's say you have MyEvent, that is persistable:
 * ```
 *  @PersistableEvent
 *  data class MyEvent(
 *      val age: Int,
 *      @Default("John") val name: String
 *  ): Event.Persistable, Event.Sticky
 * ```
 * Now this code generator will create a new database entity class:
 * ```kotlin
 * @AutoEntity
 * @Entity(tableName = "MyEventEntity", primaryKeys = ["primaryKey"])
 * data class MyEventEntity(
 *     @ColumnInfo(
 *         name = "age",
 *         defaultValue = "0"
 *     )
 *     val age: Int,
 *     @ColumnInfo(
 *         name = "name",
 *         defaultValue = "John"
 *     )
 *     val name: String,
 *     @ColumnInfo(
 *         name = "primaryKey"
 *     )
 *     val primaryKey: Int
 * )
 * ```
 */
class EntityGenerator(private val entity: PersistableEntityKSClass) : FileGenerator {

    /**
     * Generates a file containing the class code for the entity.
     *
     * @return [FileSpec] instance containing the generated code.
     */
    override fun generate(): FileSpec {
        val packageName = entity.packageName
        val className = entity.entityActualClassName
        return FileSpec.builder(packageName, className)
            .addType(buildEntityType(className))
            .build()
    }

    /**
     * Builds a [TypeSpec] object that includes class signature, annotation,
     * constructor and properties for the Room's Entity annotation version of the entity.
     *
     * @param className Name of the class to be generated.
     * @return An instance of [TypeSpec] with all code details.
     */
    private fun buildEntityType(className: String): TypeSpec {
        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)
            .addAnnotation(AutoEntity::class)
            .addAnnotation(buildEntityAnnotation())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(buildParameters())
                    .build()
            )
            .addProperties(buildProperties())
            .build()
    }

    /**
     * Builds a [AnnotationSpec] object that includes the annotation for the Room's Entity.
     * This method prepares a list of primary keys for the Entity and constructs the Entity annotation.
     *
     * @return An instance of [AnnotationSpec] with Entity annotation details.
     */
    private fun buildEntityAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(Entity::class)
            .addMember(
                "primaryKeys = [${entity.primaryKeysPoetFormat}]",
                *entity.primaryKeyProperties
            )
            .addMember("tableName = \"%L\"", entity.tableName)
            .build()
    }

    /**
     * Builds a list of [ParameterSpec] objects that includes all parameters of the primary constructor.
     *
     * @return A list of [ParameterSpec] objects.
     */
    private fun buildParameters(): List<ParameterSpec> {
        val parameters = entity.declaredParameters().toMutableList()
        if (!entity.hasDefinedPrimaryKey()) {
            parameters.add(entity.buildDefaultPrimaryKeyParameter())
        }
        return parameters
    }

    /**
     * Builds a list of [PropertySpec] objects that includes properties of class.
     *
     * @return A list of [PropertySpec] objects.
     */
    private fun buildProperties(): List<PropertySpec> {
        val properties = entity.declaredProperties().toMutableList()
        if (!entity.hasDefinedPrimaryKey()) {
            properties.add(entity.buildDefaultPrimaryKeyProperty())
        }
        return properties
    }
}