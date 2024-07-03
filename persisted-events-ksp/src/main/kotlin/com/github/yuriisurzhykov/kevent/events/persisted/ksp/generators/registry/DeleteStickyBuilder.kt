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

class DeleteStickyBuilder : RegistryFunctionBuilder {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("deletePersisted")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(
                com.squareup.kotlinpoet.TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.Sticky::class.asTypeName()
                )
            )
            .addParameter(buildClassParameter())
            .addAnnotation(unusedUncheckedCast())
            .beginControlFlow("when (%L)", RegistryFunctionBuilder.CLASS_PARAM_NAME)
            .apply {
                registry.persistableEvents.forEach { event ->
                    if (event.derivedFromComponent(Event.Sticky::class)) {
                        addCode(deletePersistedStickyCode(event))
                    }
                }
            }
            .endControlFlow()
            .build()
    }

    private fun deletePersistedStickyCode(event: DaoKSClass): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("%T::class ->", event.declarationName)
            .addStatement(
                "%L.%L().%L()",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.DELETE_BY_KEY.methodName
            )
            .endControlFlow()
            .build()
    }
}