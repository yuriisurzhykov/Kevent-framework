package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators

import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableMapperKSClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * A class used to generate entity mapper code using KotlinPoet for a given entity.
 *
 * This class aid in generating Kotlin code for mapping between SQL entities and user defined event classes. It
 * generates a FileSpec object which produces a ready-to-compile Kotlin source file when written into a file.
 *
 * Generated code may look like this:
 * ```kotlin
 *  class YourEntityMapper {
 *      fun toEntity(event: YourEvent): YourEntity {
 *          return YourEntity(
 *              event.someField1,
 *              event.someField2
 *          )
 *      }
 *
 *      fun fromEntity(entity: YourEntity): YourEvent {
 *          return YourEvent(
 *              entity.someField1,
 *              entity.someField2
 *          )
 *      }
 *  }
 * ```
 */
class EntityMapperGenerator(private val entity: PersistableMapperKSClass) : FileGenerator {

    /**
     * Generates Kotlin source code for entity mapper in the form of a FileSpec object.
     * @return FileSpec object containing the generated Kotlin source code.
     */
    override fun generate(): FileSpec {
        return FileSpec.builder(entity.packageName, entity.mapperActualClassName)
            .addType(
                TypeSpec.classBuilder(entity.mapperActualClassName)
                    .addFunction(FunSpec.constructorBuilder().build())
                    .addMapToEntityMethod()
                    .addMapFromEntityMethod()
                    .build()
            )
            .build()
    }

    /**
     * Adds a 'toEntity' method implementation to the provided TypeSpec.Builder.
     * @return TypeSpec builder with the added 'toEntity' method.
     */
    private fun TypeSpec.Builder.addMapToEntityMethod(): TypeSpec.Builder {
        val paramName = "event"
        addFunction(
            FunSpec.builder("toEntity")
                .addParameter(
                    ParameterSpec.builder(paramName, entity.entityWrapper.className)
                        .build()
                )
                .returns(ClassName(entity.packageName, entity.entityWrapper.entityActualClassName))
                .addStatement(
                    "return %T(%L)",
                    ClassName(entity.packageName, entity.entityWrapper.entityActualClassName),
                    entity.mapToNames(paramName)
                )
                .build()
        )
        return this
    }

    /**
     * Adds a 'fromEntity' method implementation to the provided TypeSpec.Builder.
     * @return TypeSpec builder with the added 'fromEntity' method.
     */
    private fun TypeSpec.Builder.addMapFromEntityMethod(): TypeSpec.Builder {
        val paramName = "entity"
        addFunction(
            FunSpec.builder("fromEntity")
                .addParameter(
                    ParameterSpec.builder(
                        paramName,
                        ClassName(entity.packageName, entity.entityWrapper.entityActualClassName)
                    ).build()
                )
                .returns(entity.entityWrapper.className)
                .addStatement(
                    "return %T(%L)",
                    entity.entityWrapper.className,
                    entity.mapFromNames(paramName)
                )
                .build()
        )
        return this
    }
}