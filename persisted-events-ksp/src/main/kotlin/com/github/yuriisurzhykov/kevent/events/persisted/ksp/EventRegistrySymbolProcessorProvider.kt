package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Represents a Symbol Processor Provider creates an instance of [EventRegistrySymbolProcessor].
 */
class EventRegistrySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EventRegistrySymbolProcessor(environment.logger, environment.codeGenerator)
    }
}