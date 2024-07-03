package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

interface RegistryFunctionBuilder {

    companion object {
        internal const val INDENT = "    "
        internal const val TYPE_VAR_NAME = "T"
        internal const val KEY_TYPE_VAR_NAME = "K"
        internal const val DATABASE_PROP_NAME = "database"
        internal const val CLASS_PARAM_NAME = "clazz"
    }

    fun buildClassParameter(): ParameterSpec {
        return ParameterSpec.builder(
            CLASS_PARAM_NAME,
            KClass::class.asTypeName()
                .parameterizedBy(TypeVariableName(TYPE_VAR_NAME))
        ).build()
    }

    fun buildMethodParameters(namesOnly: Boolean = false): List<ParameterSpec> {
        return listOf(
            ParameterSpec
                .builder("key", TypeVariableName(KEY_TYPE_VAR_NAME))
                .build()
        )
    }

    fun buildDaoCallParameters(event: DaoKSClass): Array<CodeBlock> {
        return buildMethodParameters().mapIndexed { index, spec ->
            CodeBlock.builder()
                .add(
                    "%L",
                    event.getPersistable()
                        .primaryKeyProperty()[index].propertyNameWithCast(spec.name)
                )
                .build()
        }.toTypedArray()
    }

    fun buildListReturnType(): TypeName {
        return List::class.asTypeName()
            .parameterizedBy(TypeVariableName(TYPE_VAR_NAME))
    }

    fun build(registry: EventRegistryKSClass): FunSpec

}