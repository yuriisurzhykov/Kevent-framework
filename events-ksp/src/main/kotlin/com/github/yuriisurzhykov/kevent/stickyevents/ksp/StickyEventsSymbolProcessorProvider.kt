package com.github.yuriisurzhykov.kevent.stickyevents.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the StickyEventsSymbolProcessor.
 *
 * This class is used by the Kotlin Symbol Processing API to create instances
 * of StickyEventsSymbolProcessor. It serves as a factory for creating symbol processors
 * that are specifically designed to generate code for sticky events in the EventBus library.
 * Class should be registered in META-INF resources in order KSP API would be able to find this
 * provider.
 */
class StickyEventsSymbolProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates an instance of StickyEventsSymbolProcessor.
     *
     * This method is invoked by the KSP framework to obtain a new instance
     * of StickyEventsSymbolProcessor, passing the necessary environment dependencies
     * such as code generator and logger.
     *
     * @param environment The SymbolProcessorEnvironment providing necessary utilities and configurations.
     * @return A new instance of StickyEventsSymbolProcessor.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return StickyEventsSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}