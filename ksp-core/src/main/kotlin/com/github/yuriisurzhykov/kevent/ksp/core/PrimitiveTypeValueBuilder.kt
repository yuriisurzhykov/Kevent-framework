package com.github.yuriisurzhykov.kevent.ksp.core

import com.squareup.kotlinpoet.CodeBlock

class PrimitiveTypeValueBuilder(
    private val useShortNames: Boolean,
    private val defaultProcessor: DefaultAnnotationProcessor = DefaultAnnotationProcessor.Base(
        useShortNames = useShortNames
    )
) : TypeConstructorStringBuilder {

    override fun build(
        valueParam: PropertyKSClass,
        defaultString: String?,
        trimValues: Boolean
    ): CodeBlock {
        val paramInitializerBuilder = CodeBlock.builder()

        val defaultAnnotation = valueParam.defaultAnnotation
        if (defaultAnnotation != null) {
            return defaultProcessor.buildDefault(valueParam, defaultAnnotation)
        } else if (valueParam.hasDefault) {
            return paramInitializerBuilder.build()
        } else if (valueParam.isNullable) {
            paramInitializerBuilder.add("null")
        } else if (valueParam.type.isEnum()) {
            paramInitializerBuilder.add(
                "%L",
                TypeConstructorStringBuilder.EnumBuilder()
                    .build(valueParam, trimValues = useShortNames)
            )
        } else {
            val typeName = valueParam.typeStringName
            val typeInitializer = MapOfBasicTypes[typeName]?.build(
                valueParam,
                trimValues = useShortNames
            )
            paramInitializerBuilder.add("%L", typeInitializer)
        }
        return paramInitializerBuilder.build()
    }
}