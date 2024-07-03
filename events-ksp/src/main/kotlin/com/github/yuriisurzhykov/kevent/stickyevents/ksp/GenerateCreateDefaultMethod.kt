package com.github.yuriisurzhykov.kevent.stickyevents.ksp

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.codegen.NoDefaultEventException
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.github.yuriisurzhykov.kevent.ksp.core.PropertyKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.TypeConstructorStringBuilder
import com.github.yuriisurzhykov.kevent.ksp.core.containsSuperType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

interface GenerateCreateDefaultMethod {

    /**
     * Builds the produce function for the factory class that creates instances of the specified
     * sticky events based on their class type. It adds common parameters to the produce method.
     * And generates method body with when statement to check kClass type and return event instance
     * with defaults.
     *
     * @param events The list of KSClassDeclarations representing the sticky events.
     * @return A [FunSpec.Builder] object representing the produce function.
     */
    fun method(events: Sequence<KSClassDeclaration>): FunSpec

    abstract class Abstract(
        private val logger: KSPLogger,
        private val baseClass: KClass<out Event>,
        private val methodName: String = "createDefault",
        private val classParamName: String = "kClass"
    ) : GenerateCreateDefaultMethod {

        protected open fun classParameter(): TypeName {
            return KClass::class.asTypeName().parameterizedBy(TypeVariableName("T"))
        }

        protected open fun filterEvents(events: Sequence<KSClassDeclaration>): Sequence<KSClassDeclaration> =
            events.filter { it.containsSuperType(baseClass) }

        protected open fun buildTypeVariable(): TypeName = baseClass.asTypeName()

        override fun method(events: Sequence<KSClassDeclaration>): FunSpec {
            // generate the 'produce' method
            return FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                // Type variable is a generic type which applies to a method
                // and in code it look the following: fun <T: type> methodName(...)
                .addTypeVariable(TypeVariableName("T", buildTypeVariable()))
                // Add KClass as a parameter
                .addParameter(classParamName, classParameter())
                .returns(TypeVariableName("T"))
                .beginControlFlow("return when($classParamName)")
                .apply {
                    filterEvents(events).forEach { event ->
                        addStatement(
                            "%T::class -> %L as T",
                            event.toClassName(),
                            buildConstructorCall(event)
                        )
                    }
                }
                // Generate throw exception if the provided class has no @StickyComponent
                // annotation and there is no branch to create an instance of this class.
                .addCode(
                    CodeBlock.of(
                        "else -> throw %T(%L)\n",
                        NoDefaultEventException::class,
                        classParamName
                    )
                )
                .endControlFlow()
                .build()
        }

        /**
         * Builds a constructor call for future factory file for a given class type.
         *
         * @param classType The KSClassDeclaration of the class for which to build a constructor call.
         * @return A string representing the constructor call.
         */
        private fun buildConstructorCall(classType: KSClassDeclaration): CodeBlock {
            return TypeConstructorStringBuilder.CustomTypeBuilder(logger)
                .build(PropertyKSClass(classType), trimValues = true)
        }
    }

    /**
     * Generates `produce` method for [Event.Sticky] classes.
     * */
    class CreateDefaultSticky(logger: KSPLogger) : Abstract(logger, Event.Sticky::class)

    /**
     * Generates `produce` method for [Event.StickyCollection] classes.
     * */
    class CreateDefaultStickyCollection(logger: KSPLogger) :
        Abstract(logger, Event.StickyCollection::class) {
        override fun buildTypeVariable(): TypeName =
            Event.StickyCollection::class.asTypeName().parameterizedBy(STAR)
    }
}