package com.github.yuriisurzhykov.kevent.events.persisted.ksp.utils

import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry.RegistryFunctionBuilder.Companion.TYPE_VAR_NAME
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

internal fun anyTypeVariable(typeName: String = TYPE_VAR_NAME) = TypeVariableName(
    typeName,
    Any::class.asTypeName()
)