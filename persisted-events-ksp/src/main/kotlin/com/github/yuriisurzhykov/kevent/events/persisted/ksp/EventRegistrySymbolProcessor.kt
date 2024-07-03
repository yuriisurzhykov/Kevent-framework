package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.github.yuriisurzhykov.kevent.events.persisted.core.database.AutoDatabase
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.EventRegistry
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEvent
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEventRegistry
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.registry.EventRegistryGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.AutoDatabaseKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.EventRegistryKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.getSymbols
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Represents a symbol processor that processes classes annotated with [EventRegistry], [AutoDatabase], and
 * [PersistableEvent] annotations to generate a [PersistableEventRegistry] implementation.
 *
 * @property logger The logger used for logging errors and messages during processing.
 * @property codeGenerator The code generator used to generate the output files.
 */
class EventRegistrySymbolProcessor(
    logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : AbstractSymbolProcessor(logger) {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Receive all classes annotated with @EventRegistry, @AutoDatabase and
        // @PersistedEvent in order to generate PersistableEventRegistry implementation.
        val databases = resolver.getSymbols(AutoDatabase::class).groupBy(KSNode::validate)
        val persistableEvents =
            resolver.getSymbols(PersistableEvent::class).groupBy(KSNode::validate)

        // Creates a list only for valid source set, which is ready to be processed.
        val validPersistableEvents = persistableEvents[true].orEmpty().toList()
        val validDatabases = databases[true].orEmpty().toList()

        val persistableFilesToDependOn =
            persistableEvents.flatMap { it.value }.mapNotNull { it.containingFile }

        val databaseFilesToDependOn =
            databases.asSequence().flatMap { it.value }.mapNotNull { it.containingFile }

        val filesToDependOn =
            (persistableFilesToDependOn + databaseFilesToDependOn).toTypedArray()

        if (validDatabases.isNotEmpty()) {
            // Using forEach instead of databases[0] because of weird behavior of KSP: it cannot
            // find element under index 0, but can iterate using forEach ext function.
            validDatabases.forEach { dbDeclaration ->
                generateRegistry(dbDeclaration, validPersistableEvents)
                    .writeTo(codeGenerator, Dependencies(aggregating = true, *filesToDependOn))
            }
        } else {
            logger.warn("Cannot find event registry or database for registry! Databases ${databases.size}")
        }

        // Returns registry classes that are not valid for current round of processing.
        return databases[false].orEmpty()
    }

    /**
     * Generates an event registry for events marked as persistable to save their respective entities.
     *
     * @param autoDatabase The KSClassDeclaration representing the auto database.
     * @param persistableEvents The list of KSClassDeclarations representing persistable events.
     *
     * @return The [FileSpec] representing the generated registry.
     */
    private fun generateRegistry(
        autoDatabase: KSClassDeclaration,
        persistableEvents: List<KSClassDeclaration>
    ): FileSpec {
        val daoEvents = persistableEvents.map { DaoKSClass(it) }
        val eventRegistry =
            EventRegistryKSClass(
                AutoDatabaseKSClass(autoDatabase),
                daoEvents,
                PersistableEventRegistry::class
            )

        return EventRegistryGenerator(eventRegistry).generate()
    }
}