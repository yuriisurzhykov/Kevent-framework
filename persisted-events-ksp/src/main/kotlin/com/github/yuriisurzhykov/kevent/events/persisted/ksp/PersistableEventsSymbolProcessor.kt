package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEvent
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.DaoInterfaceGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.EntityGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.EntityMapperGenerator
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableEntityKSClass
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.PersistableMapperKSClass
import com.github.yuriisurzhykov.kevent.ksp.core.getSymbols
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Represents a symbol processor that processes classes annotated with the @[PersistableEvent] annotation.
 *
 * @property codeGenerator The code generator used to generate code.
 * @property logger The logger used to log errors and messages during processing.
 */
class PersistableEventsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    logger: KSPLogger
) : AbstractSymbolProcessor(logger) {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Getting all classes annotated with @PersistableEvent annotation
        // and validating them, to process only valid sources.
        val persistableEvents =
            resolver.getSymbols(PersistableEvent::class).groupBy(KSNode::validate)

        val validPersistableEvents = persistableEvents[true].orEmpty().toList()

        // Walk through all events with annotations, and generate corresponding DAO, entity and mapper
        // for the given event.
        validPersistableEvents.forEach { declaration ->
            // Check the requirement for event to be persisted.
            // It has to be inherited from Event.Persistable, and from either Event.Sticky
            // or Event.StickyCollection
            checkDeclarationHasType(
                declaration,
                PersistableEvent::class,
                Event.Persistable::class,
                Event.Sticky::class,
                Event.StickyCollection::class,
            )
            // Wrapping the class declaration into custom classes, for more convenient way of
            // working with different class specifications.
            val persistableEntity = PersistableEntityKSClass(declaration)
            val fileToDepend =
                declaration.containingFile?.let { arrayOf(it) } ?: emptyArray<KSFile>()

            // Generating entity class
            EntityGenerator(persistableEntity).generate()
                .writeTo(codeGenerator, Dependencies(aggregating = true, *fileToDepend))

            // Generating mapper class
            EntityMapperGenerator(PersistableMapperKSClass(declaration)).generate()
                .writeTo(codeGenerator, Dependencies(aggregating = true, *fileToDepend))

            // Generating DAO class
            DaoInterfaceGenerator(DaoKSClass(declaration)).generate()
                .writeTo(codeGenerator, Dependencies(aggregating = true, *fileToDepend))
        }

        // Returns all event classes which are not ready to be processed, to process
        // in the next processing round.
        return persistableEvents[false].orEmpty()
    }
}