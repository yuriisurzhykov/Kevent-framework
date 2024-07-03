package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.utils.anyTypeVariable
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.unusedUncheckedCast
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class DeleteCollectionBuilder : RegistryFunctionBuilder {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("clearPersistedCollection")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(anyTypeVariable(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME))
            .addTypeVariable(
                TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.Iterable::class.asTypeName().parameterizedBy(
                        TypeVariableName(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME)
                    )
                )
            )
            .addParameter(buildClassParameter())
            .addAnnotation(unusedUncheckedCast())
            .beginControlFlow("when (%L)", RegistryFunctionBuilder.CLASS_PARAM_NAME)
            .apply {
                registry.persistableEvents.forEach { event ->
                    if (event.derivedFromComponent(Event.Iterable::class)) {
                        addCode(deletePersistedStickyCollectionCode(event))
                    }
                }
            }
            .endControlFlow()
            .build()
    }

    private fun deletePersistedStickyCollectionCode(event: DaoKSClass): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("%T::class ->", event.declarationName)
            .addStatement(
                "%L.%L().%L()",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.CLEAR_TABLE.methodName,
            )
            .endControlFlow()
            .build()
    }
}