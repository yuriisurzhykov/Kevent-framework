package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry

import com.github.yuriisurzhykov.kevent.events.persisted.core.events.EventRegistry
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.FileGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry.RegistryFunctionBuilder.Companion.DATABASE_PROP_NAME
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * This class generates a registry for event processing, that implements a set of required methods,
 * such as persisting an event, acquiring persisted sticky event and acquiring persisted sticky collection event.
 * The generated code utilizes the auto-generated database class, to have access to its DAO getters to persist
 * event entity and to read stored event entities.
 *
 * @property registry [EventRegistryKSClass] that is a wrapper for class annotated with @[EventRegistry] annotation
 * for more convenient way to work with KSDeclaration.
 */
class EventRegistryGenerator(
    private val registry: EventRegistryKSClass
) : FileGenerator {

    /**
     * Generates a full kotlin file with the generated EventRegistry class,
     * fully implemented with persisting methods and acquisition methods for sticky events.
     *
     * @return The generated Kotlin file.
     */
    override fun generate(): FileSpec {
        return FileSpec.builder(registry.packageName, registry.actualClassName)
            .addType(
                TypeSpec.classBuilder(registry.actualClassName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder(DATABASE_PROP_NAME, registry.databaseClass)
                                    .build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(DATABASE_PROP_NAME, registry.databaseClass)
                            .mutable(false).addModifiers(KModifier.PRIVATE)
                            .initializer(DATABASE_PROP_NAME)
                            .build()
                    )
                    .addSuperinterface(registry.declarationClassName)
                    .addFunction(PersistMethodBuilder().build(registry))
                    .addFunction(ReadPersistedStickyBuilder().build(registry))
                    .addFunction(ReadPersistedStickyCollectionBuilder().build(registry))
                    .addFunction(ReadPersistedCollectionBuilder().build(registry))
                    .addFunction(DeleteStickyBuilder().build(registry))
                    .addFunction(DeleteStickyCollectionBuilder().build(registry))
                    .addFunction(DeleteCollectionBuilder().build(registry))
                    .build()
            )
            .build()
    }
}