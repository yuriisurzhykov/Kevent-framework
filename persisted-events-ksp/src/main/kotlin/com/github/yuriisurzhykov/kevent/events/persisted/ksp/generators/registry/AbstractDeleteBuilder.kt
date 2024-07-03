package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.squareup.kotlinpoet.CodeBlock

abstract class AbstractDeleteBuilder : RegistryFunctionBuilder {
    protected open fun deletePersistedEventCodeBlock(event: DaoKSClass): CodeBlock {
        val keyParametersStringFormat = buildMethodParameters().joinToString { "%L" }
        val parameters = buildDaoCallParameters(event)
        return CodeBlock.builder()
            .beginControlFlow("%T::class ->", event.declarationName)
            .addStatement(
                "%L.%L().%L($keyParametersStringFormat)",
                RegistryFunctionBuilder.DATABASE_PROP_NAME,
                event.getDaoMethodName(),
                DaoMethod.DELETE_BY_KEY.methodName,
                *parameters
            )
            .endControlFlow()
            .build()
    }
}