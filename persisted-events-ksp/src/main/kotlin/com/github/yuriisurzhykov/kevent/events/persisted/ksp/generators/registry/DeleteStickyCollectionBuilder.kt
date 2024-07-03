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

class DeleteStickyCollectionBuilder : AbstractDeleteBuilder() {
    override fun build(registry: EventRegistryKSClass): FunSpec {
        return FunSpec.builder("deletePersisted")
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .addTypeVariable(anyTypeVariable(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME))
            .addTypeVariable(
                TypeVariableName(
                    RegistryFunctionBuilder.TYPE_VAR_NAME,
                    Event.StickyCollection::class.asTypeName().parameterizedBy(
                        TypeVariableName(RegistryFunctionBuilder.KEY_TYPE_VAR_NAME)
                    )
                )
            )
            .addParameter(buildClassParameter())
            .addParameters(buildMethodParameters())
            .addAnnotation(unusedUncheckedCast())
            .beginControlFlow("when (%L)", RegistryFunctionBuilder.CLASS_PARAM_NAME)
            .apply {
                registry.persistableEvents.forEach { event ->
                    if (event.derivedFromComponent(Event.StickyCollection::class)) {
                        addCode(deletePersistedEventCodeBlock(event))
                    }
                }
            }
            .endControlFlow()
            .build()
    }
}