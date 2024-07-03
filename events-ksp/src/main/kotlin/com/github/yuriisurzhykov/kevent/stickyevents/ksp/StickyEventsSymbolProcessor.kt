package com.github.yuriisurzhykov.kevent.stickyevents.ksp

import com.github.yuriisurzhykov.kevent.events.codegen.StickyFactoryComponent
import com.github.yuriisurzhykov.kevent.events.codegen.DefaultableStickyEvent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.github.yuriisurzhykov.kevent.ksp.core.getSymbols
import com.github.yuriisurzhykov.kevent.ksp.core.unusedUncheckedCast
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * **THIS CLASS SHOULD NEVER BY USED IN ACTUAL CODE!**
 *
 * `StickyEventsSymbolProcessor` is responsible for generating factory class implementations for sticky events.
 *
 * This processor fetches the classes annotated with the StickyComponent and FactoryComponent annotations, then it
 * generates a factory class that can create the instances of those sticky events at compile time.
 *
 * For instance, if you have a class marked with the StickyComponent annotation:
 * ```
 * @StickyComponent
 * class ExampleStickyEvent : Event()
 * ```
 * The processor will generate a factory class with methods to produce instances of those sticky events.
 */
class StickyEventsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * Logs a system error message using the KSPLogger when an unexpected error occurs during processing.
     */
    override fun onError() {
        logger.error("Sticky symbol processor. Unexpected error occurred during processing.")
    }

    /**
     * Finds classes annotated with `DefaultableStickyEvent` and `FactoryComponent`, then invokes `generateFactory` method to
     * generate corresponding factory class for sticky events with default values.
     *
     * This method doesn't produce any additional symbols to be processed, hence it will always return an empty list.
     *
     * @param resolver Resolver to fetch class declarations using specific annotations.
     * @return An empty list, as this processor does not produce additional symbols to be processed.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Searching all classes marked with StickyComponent annotation
        val stickyElements = resolver.getSymbols(DefaultableStickyEvent::class).groupBy(KSNode::validate)
        val factories = resolver.getSymbols(StickyFactoryComponent::class).groupBy(KSNode::validate)
        val validStickyElements = stickyElements[true].orEmpty().asSequence()
        val validFactories = factories[true].orEmpty().asSequence()
        // Searching all classes marked with FactoryComponent annotation
        val stickyEventsFiles = stickyElements
            .flatMap { it.value }
            .mapNotNull { it.containingFile }.toList()
        validFactories.forEach { declaration ->
            val factoryDescription = generateFactory(declaration.toClassName(), validStickyElements)
            logger.info("Factory structure generated: $factoryDescription.")
            val filesToDependOn =
                if (declaration.containingFile != null) (stickyEventsFiles + declaration.containingFile!!)
                else stickyEventsFiles
            factoryDescription.writeTo(
                codeGenerator,
                Dependencies(aggregating = true, *filesToDependOn.toTypedArray())
            )
        }
        return stickyElements[false].orEmpty() + factories[false].orEmpty()
    }

    /**
     * Uses `KotlinPoet` to generate a `FileSpec` that represents the factory class for given sticky events and
     * factory interface.
     *
     * For example, if you have `ExampleStickyEvent` with string and int parameter, it will create a factory method:
     * ```
     * override fun <T : Event.Sticky> produce(kClass: KClass<T>): T {
     *     return when (kClass) {
     *         ExampleStickyEvent::class -> ExampleStickyEvent("", 0) as T
     *     }
     * }
     * ```
     *
     * @param factory The KSClassDeclaration representing the factory interface.
     * @param events The list of KSClassDeclarations representing sticky events.
     * @return A FileSpec object representing the generated factory class.
     */
    private fun generateFactory(
        factory: ClassName,
        events: Sequence<KSClassDeclaration>
    ): FileSpec {
        // Trying to access package information for abstract sticky factory
        val packageName = factory.packageName
        logger.info("Sticky event factory package: $packageName")
        // Creating generated sticky factory class name
        val factoryName = factory.simpleName + "Impl"
        logger.info("Sticky event factory name: $factoryName")
        // Building the structure of generated sticky factory
        return FileSpec.builder(packageName, factoryName)
            // Type in code generator means 'class', 'interface', 'enum', etc.
            // For this case we generate class
            .addType(
                TypeSpec
                    .classBuilder(factoryName)
                    // Annotate generated class with 'UNCHECKED_CAST' in order android lint
                    // won't print messages about casting types to generic T
                    .addAnnotation(unusedUncheckedCast())
                    // Appending implementation interface as abstract client factory.
                    .addSuperinterface(factory)
                    // Create constructor
                    .addFunction(FunSpec.constructorBuilder().build())
                    // Create produce method for events derived from Event.Sticky
                    .addFunction(GenerateCreateDefaultMethod.CreateDefaultSticky(logger).method(events))
                    // Create produce method for events derived from Event.StickyCollection
                    .addFunction(
                        GenerateCreateDefaultMethod.CreateDefaultStickyCollection(logger).method(events)
                    )
                    .build()
            )
            .build()
    }
}