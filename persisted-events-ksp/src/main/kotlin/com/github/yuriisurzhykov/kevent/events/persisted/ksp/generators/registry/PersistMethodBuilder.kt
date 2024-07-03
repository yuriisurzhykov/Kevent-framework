package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class PersistMethodBuilder : RegistryFunctionBuilder {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("persist")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(
                TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.Persistable::class.asTypeName()
                )
            )
            .addParameter(
                ParameterSpec.builder(
                    "event",
                    TypeVariableName(RegistryFunctionBuilder.TYPE_VAR_NAME)
                ).build()
            )
            .beginControlFlow("when(event)")
            .apply {
                registry.persistableEvents.forEach { event ->
                    addCode(writePersistCodeBlock(event))
                }
            }
            .endControlFlow()
            .build()
    }

    /**
     * Creates a CodeBlock to persist an event into the database.
     *
     * @param event Event class to process.
     * @return CodeBlock instance that persists data to the database.
     */
    private fun writePersistCodeBlock(event: DaoKSClass): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("is %T ->", event.declarationName)
            .add(
                "val mappedEntity = %T().toEntity(event)\n",
                event.mapperName
            )
            .add(
                "%L.%L().%L(mappedEntity)\n",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.UPSERT.methodName
            )
            .endControlFlow()
            .build()
    }
}