package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.AutoDao
import com.github.yuriisurzhykov.kevent.events.persisted.core.database.AutoDatabase
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.AutoEntity
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEvent
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.DatabaseGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.AutoDatabaseKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
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
 * Represents a symbol processor that processes classes annotated with the [AutoDatabase] annotation.
 *
 * @property logger The logger used for logging errors and messages during processing.
 * @property codeGenerator The code generator used for generating code.
 */
class AutoDatabaseSymbolProcessor(
    logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : AbstractSymbolProcessor(logger) {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Find all classes annotated with @AutoDatabase, and group them by
        // validity flag.
        val databases = resolver.getSymbols(AutoDatabase::class).groupBy(KSNode::validate)

        val validDatabases = databases[true].orEmpty().toList()

        // If there is more than one instance of auto generated database, create error
        // because it would not be possible to create event registry that utilizes different
        // types of database.
        if (validDatabases.size > 1) {
            logger.error("Cannot be more then 1 @AutoDatabase in the project. Carefully review your sources, and try to avoid multiple @AutoDatabases!")
        }

        // Receive the list of events annotated with @PersistableEvent to be persisted
        val persistableEvents = resolver.getSymbols(PersistableEvent::class).toList()

        // Receive a list of auto-generated DAOs and entities to create a list of
        // files for auto-generated database to depend on.
        val daos = resolver.getSymbols(AutoDao::class).toList()
        val entities = resolver.getSymbols(AutoEntity::class).toList()
        val dependenciesFiles =
            (daos.mapNotNull { it.containingFile } + entities.mapNotNull { it.containingFile }).toTypedArray()

        // Walking the single item list of databases and generate database impl.
        // Use forEach method for database even after check for only one instance
        // because KSP cannot read validDatabases[0] for some reason, and work only
        // with iterators.
        validDatabases.forEach { dbDeclaration ->
            generateDatabase(dbDeclaration, persistableEvents).writeTo(
                codeGenerator,
                Dependencies(aggregating = true, *dependenciesFiles)
            )
        }

        // Returns database classes that are not valid for current round of processing.
        return databases[false].orEmpty()
    }

    /**
     * Generates a database FileSpec based on the given database declaration and list of entities used for DAO
     * references.
     *
     * @param databaseDeclaration The database class declaration represented as a KSClassDeclaration.
     * @param entitiesForDao The list of entities for DAOs represented as a list of KSClassDeclaration objects.
     * @return The generated FileSpec representing the database class.
     */
    private fun generateDatabase(
        databaseDeclaration: KSClassDeclaration,
        entitiesForDao: List<KSClassDeclaration>
    ): FileSpec {
        val entitiesSpecs = entitiesForDao.map { DaoKSClass(it) }
        return DatabaseGenerator(AutoDatabaseKSClass(databaseDeclaration), entitiesSpecs)
            .generate()
    }
}