package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.squareup.kotlinpoet.CodeBlock

abstract class AbstractReadBuilder : RegistryFunctionBuilder {

    protected open fun getPersistedElseBranch(): CodeBlock {
        return CodeBlock.of("else -> emptyList()")
    }

    protected open fun getPersistedStickyCodeBlock(event: DaoKSClass): CodeBlock {
        val keyParametersStringFormat = buildMethodParameters().joinToString { "%L" }
        val parameters = buildDaoCallParameters(event)
        return CodeBlock.builder()
            .beginControlFlow("%T::class ->", event.declarationName)
            .add(
                "val entities = %L.%L().%L(${keyParametersStringFormat})\n",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.GET_BY_KEY.methodName,
                *parameters
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