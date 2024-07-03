package com.github.yuriisurzhykov.kevent.events.persisted.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Represents a KSP provider that creates an instance of the [AutoDatabaseSymbolProcessor].
 */
class AutoDatabaseSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoDatabaseSymbolProcessor(environment.logger, environment.codeGenerator)
    }
}