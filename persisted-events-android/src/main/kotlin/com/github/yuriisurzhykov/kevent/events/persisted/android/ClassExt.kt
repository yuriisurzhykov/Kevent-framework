package com.github.yuriisurzhykov.kevent.events.persisted.android

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> KClass<T>.generatedClass(): Class<T> {
    val klass = java
    // Extracting the package and class name information.
    val fullPackage = klass.`package`?.name.orEmpty()
    val name = klass.canonicalName.orEmpty()
    val postPackageName =
        if (fullPackage.isEmpty()) name else name.substring(fullPackage.length + 1)

    // Constructing the implementation class name.
    val implName = postPackageName + "Impl"

    // Constructing the full class name and creating an instance of the factory.
    val fullClassName = if (fullPackage.isEmpty())
        implName
    else "$fullPackage.$implName"
    return Class.forName(fullClassName, true, klass.classLoader) as Class<T>
}