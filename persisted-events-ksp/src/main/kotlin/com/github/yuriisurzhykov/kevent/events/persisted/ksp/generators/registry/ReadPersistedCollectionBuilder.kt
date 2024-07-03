package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.utils.anyTypeVariable
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.unusedUncheckedCast
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class ReadPersistedCollectionBuilder : ReadPersistedStickyBuilder() {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("readPersistedCollection")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(anyTypeVariable(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME))
            .addTypeVariable(
                TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.Iterable::class.asTypeName()
                        .parameterizedBy(TypeVariableName(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME))
                )
            )
            .addParameter(buildClassParameter())
            .addAnnotation(unusedUncheckedCast())
            .returns(buildListReturnType())
            .beginControlFlow("return when (%L)", RegistryFunctionBuilder.CLASS_PARAM_NAME)
            .apply {
                registry.persistableEvents.forEach { event ->
                    if (event.derivedFromComponent(Event.Iterable::class)) {
                        addCode(getPersistedStickyCodeBlock(event))
                    }
                }
            }
            .addStatement("else -> throw IllegalArgumentException(\"Class \$${RegistryFunctionBuilder.CLASS_PARAM_NAME} is not allowed to be read as persisted\")")
            .endControlFlow()
            .build()
    }
}