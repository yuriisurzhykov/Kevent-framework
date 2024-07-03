package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators

import androidx.room.Query
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableEntityKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableEntityKSClass.Companion.DEFAULT_PRIMARY_KEY_NAME
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableEntityKSClass.Companion.DEFAULT_PRIMARY_KEY_VALUE
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

fun DaoMethodGenerator(daoMethod: DaoMethod): DaoMethodGenerator =
    when (daoMethod) {
        DaoMethod.INSERT -> DaoMethodGenerator.Insert()
        DaoMethod.UPDATE -> DaoMethodGenerator.Update()
        DaoMethod.DELETE -> DaoMethodGenerator.Delete()
        DaoMethod.DELETE_BY_KEY -> DaoMethodGenerator.DeleteByKey()
        DaoMethod.UPSERT -> DaoMethodGenerator.Upsert()
        DaoMethod.GET_BY_KEY -> DaoMethodGenerator.GetByKey()
        DaoMethod.GET_ALL -> DaoMethodGenerator.GetAll()
        DaoMethod.CLEAR_TABLE -> DaoMethodGenerator.ClearTable()
        DaoMethod.INSERT_ALL -> DaoMethodGenerator.InsertAll()
        DaoMethod.GET_LIMIT_1 -> DaoMethodGenerator.GetLimit1()
    }

interface DaoMethodGenerator {

    fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec

    /**
     * Represents an abstract class for generating Dao methods based on annotations.
     *
     * @param annotationUse The annotation class to be used as a method annotation.
     * @param paramName The name of the parameter for the generated method.
     */
    abstract class AnnotationBasedMethod(
        private val annotationUse: KClass<out Annotation>,
        private val paramName: String
    ) : DaoMethodGenerator {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            return FunSpec.builder(daoMethod.methodName)
                .addParameter(paramName, clazz.entityClassName)
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(annotationUse)
                .build()
        }
    }

    /**
     * Generates `insert` Dao method with @[androidx.room.Insert] annotation.
     * The final look for the generated method is:
     * ```
     * @Insert
     * suspend fun insert(entity: <Entity type>)
     * ```
     */
    class Insert : AnnotationBasedMethod(
        androidx.room.Insert::class,
        "entity"
    )

    /**
     * Generates `update` Dao method with @[androidx.room.Update] annotation.
     * The final look for the generated method is:
     * ```
     * @Update
     * suspend fun update(entity: <Entity type>)
     * ```
     */
    class Update : AnnotationBasedMethod(
        androidx.room.Update::class,
        "entity"
    )

    /**
     * Generates `delete` Dao method with @[androidx.room.Delete] annotation.
     * The final look for the generated method is:
     * ```
     * @Delete
     * suspend fun delete(entity: <Entity type>)
     * ```
     */
    class Delete : AnnotationBasedMethod(
        androidx.room.Delete::class,
        "entity"
    )

    /**
     * Generates `upsert` Dao method with @[androidx.room.Upsert] annotation.
     * The final look for the generated method is:
     * ```
     * @Upsert
     * suspend fun upsert(entity: <Entity type>)
     * ```
     */
    class Upsert : AnnotationBasedMethod(
        androidx.room.Upsert::class,
        "entity"
    )

    /**
     * Generates `upsertAll` Dao method with @[androidx.room.Insert] annotation.
     * The final look for the generated method is:
     * ```
     * @Insert
     * suspend fun upsertAll(entity: List<Entity type>)
     * ```
     */
    class InsertAll : DaoMethodGenerator {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            return FunSpec.builder(daoMethod.methodName)
                .addParameter(
                    "entities",
                    List::class.asTypeName().parameterizedBy(clazz.entityClassName)
                )
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(androidx.room.Insert::class)
                .build()
        }
    }

    /**
     * Abstract class to generate DAO methods for which there should be a way to get the table name for the
     * entity and the primary key name.
     * */
    abstract class QueryBasedMethod : DaoMethodGenerator {
        protected open fun tableName(clazz: PersistableEntityKSClass): String {
            return clazz.resolveTableName()
        }

        protected fun whereFilterQueryStatement(
            clazz: PersistableEntityKSClass,
            funcBuilder: FunSpec.Builder
        ): CodeBlock {
            val filterCodeBlock = CodeBlock.builder()
            val entityKeyProperties = clazz.primaryKeyProperty()
            if (entityKeyProperties.isNotEmpty()) {
                entityKeyProperties.forEachIndexed { index, param ->
                    funcBuilder.addParameter(param.propertyName, param.className)
                    val sqlName =
                        if (param.sqlPropertyName == "key") "`key`" else param.sqlPropertyName

                    filterCodeBlock.add("%L=:%L", sqlName, param.propertyName)
                    if (index < entityKeyProperties.size - 1) {
                        filterCodeBlock.add(" AND ")
                    }
                }
            } else {
                filterCodeBlock.add("%L=%L", DEFAULT_PRIMARY_KEY_NAME, DEFAULT_PRIMARY_KEY_VALUE)
            }
            return filterCodeBlock.build()
        }
    }

    /**
     * Generates a DAO's method to get entity by its key.
     * The generated method will look like:
     * ```kotlin
     * @Query("SELECT * FROM table_name WHERE key=:entityKey")
     * suspend fun getEntity(entityKey: <some type>): <Entity type>
     * ```
     */
    class GetByKey : QueryBasedMethod() {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            val funcBuilder = FunSpec.builder(daoMethod.methodName)
            val queryCodeBlock = CodeBlock.builder()
                .add("\"SELECT * FROM %L WHERE ", tableName(clazz))
                .add(whereFilterQueryStatement(clazz, funcBuilder))
                .add("\"")

            return funcBuilder
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(
                    AnnotationSpec.builder(Query::class)
                        .addMember(queryCodeBlock.build())
                        .build()
                )
                .returns(List::class.asTypeName().parameterizedBy(clazz.entityClassName))
                .build()
        }
    }

    /**
     * Generates a DAO's method to delete entity by its key.
     * The generated method will look like:
     * ```kotlin
     * @Query("DELETE FROM table_name WHERE key=:entityKey")
     * suspend fun deleteByKey(entityKey: <some type>): <Entity type>
     * ```
     */
    class DeleteByKey : QueryBasedMethod() {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            val funcBuilder = FunSpec.builder(daoMethod.methodName)
            val queryCodeBlock = CodeBlock.builder()
                .add("\"DELETE FROM %L WHERE ", tableName(clazz))
                .add(whereFilterQueryStatement(clazz, funcBuilder))
                .add("\"")

            return funcBuilder
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(
                    AnnotationSpec.builder(Query::class)
                        .addMember(queryCodeBlock.build())
                        .build()
                )
                .build()
        }
    }

    /**
     * Generates a DAO's method to get all entities from the table.
     * The generated method will look like:
     * ```kotlin
     * @Query("SELECT * FROM table_name")
     * suspend fun getAll(): List<Entity type>
     * ```
     */
    class GetAll : QueryBasedMethod() {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            return FunSpec.builder(daoMethod.methodName)
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(
                    AnnotationSpec.builder(Query::class)
                        .addMember("\"SELECT * FROM %L\"", tableName(clazz))
                        .build()
                )
                .returns(List::class.asTypeName().parameterizedBy(clazz.entityClassName))
                .build()
        }
    }

    /**
     * Generates a DAO's method to clear the entity table completely.
     * The generated method will look like:
     * ```kotlin
     * @Query("DELETE FROM table_name")
     * suspend fun clearTable()
     * ```
     */
    class ClearTable : QueryBasedMethod() {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            return FunSpec.builder(daoMethod.methodName)
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(
                    AnnotationSpec.builder(Query::class)
                        .addMember("\"DELETE FROM %L\"", tableName(clazz))
                        .build()
                )
                .build()
        }
    }

    /**
     * Generates a DAO's method to get last inserted entity to the table.
     * The generated method will look like:
     * ```kotlin
     * @Query("SELECT * FROM table_name LIMIT 1")
     * suspend fun getLast(): <Entity type>
     * ```
     */
    class GetLimit1 : QueryBasedMethod() {
        override fun generateFor(clazz: PersistableEntityKSClass, daoMethod: DaoMethod): FunSpec {
            return FunSpec.builder(daoMethod.methodName)
                .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
                .addAnnotation(
                    AnnotationSpec.builder(Query::class)
                        .addMember("\"SELECT * FROM %L LIMIT 1\"", tableName(clazz))
                        .build()
                )
                .build()
        }
    }
}