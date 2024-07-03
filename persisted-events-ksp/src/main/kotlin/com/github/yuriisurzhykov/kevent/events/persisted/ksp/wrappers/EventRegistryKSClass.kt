package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers

import com.github.yuriisurzhykov.kevent.events.persisted.core.events.PersistableEventRegistry
import com.github.yuriisurzhykov.kevent.ksp.core.IMPL_NAME
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

/**
 * Represents a wrapper for event registry class declaration for persistable events.
 *
 * @property persistableEvents The list of [DaoKSClass] representing the wrappers for persistable events DAOs
 * @property databaseClass The [ClassName] instance represents the fully qualified database class name.
 * @property actualClassName The actual class name of the registry.
 * @property packageName The package name of the registry.
 * @property declarationClassName The class name representing the registry declaration.
 */
class EventRegistryKSClass(
    autoDatabase: AutoDatabaseKSClass,
    val persistableEvents: List<DaoKSClass>,
    registryClass: KClass<out PersistableEventRegistry>
) {

    val databaseClass = autoDatabase.className
    val actualClassName = registryClass.asClassName().simpleName.plus(IMPL_NAME)
    val packageName = registryClass.asClassName().packageName
    val declarationClassName = registryClass.asClassName()
}