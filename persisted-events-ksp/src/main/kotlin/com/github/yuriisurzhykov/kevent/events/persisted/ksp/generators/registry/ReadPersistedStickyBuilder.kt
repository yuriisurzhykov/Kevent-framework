package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.unusedUncheckedCast
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName

open class ReadPersistedStickyBuilder : AbstractReadBuilder() {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("readPersisted")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(
                com.squareup.kotlinpoet.TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.Sticky::class.asTypeName()
                )
            )
            .addParameter(buildClassParameter())
            .addAnnotation(unusedUncheckedCast())
            .returns(buildListReturnType())
            .beginControlFlow("return when (%L)", RegistryFunctionBuilder.CLASS_PARAM_NAME)
            .apply {
                registry.persistableEvents.forEach { event ->
                    if (event.derivedFromComponent(Event.Sticky::class)) {
                        addCode(getPersistedStickyCodeBlock(event))
                    }
                }
            }
            .addCode(getPersistedElseBranch())
            .endControlFlow()
            .build()
    }

    /**
     * Creates a CodeBlock to get persisted sticky-event-related data from the database.
     *
     * @param event Event class to process.
     * @return CodeBlock instance that retrieves data from the database.
     */
    override fun getPersistedStickyCodeBlock(event: DaoKSClass): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("%T::class ->", event.declarationName)
            .add(
                "val entities = %L.%L().%L()\n",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.GET_ALL.methodName
            )
            .add("val mapper = %T()\n", event.mapperName)
            .add(
                "entities.map { entity -> mapper.fromEntity(entity) as %L }\n",
                RegistryFunctionBuilder.TYPE_VAR_NAME
            )
            .endControlFlow()
            .build()
    }
}