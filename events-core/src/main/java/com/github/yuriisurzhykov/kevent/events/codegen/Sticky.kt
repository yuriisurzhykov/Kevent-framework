package com.github.yuriisurzhykov.kevent.events.codegen

/**
 * Object `Sticky` is responsible for creating factory for sticky events in EventBus.
 * It provides a mechanism to generate default values for sticky events.
 */
object Sticky {

    /**
     * Creates an instance of the specified `StickyEventsFactory`.
     *
     * @param T The type of factory to be created.
     * @param klass The class of the factory to be created.
     * @return An instance of the specified factory.
     * @throws RuntimeException If an implementation for the specified factory cannot be found,
     *                          cannot be accessed, or cannot be instantiated.
     */
    @Suppress("UNCHECKED_CAST", "unused")
    fun <T : DefaultStickyEventsFactory> createFactory(klass: Class<T>): T {
        // Extracting the package and class name information.
        val fullPackage = klass.`package`.name
        val name = klass.canonicalName
        val postPackageName =
            if (fullPackage.isEmpty()) name else name.substring(fullPackage.length + 1)

        // Constructing the implementation class name.
        val implName = postPackageName + "Impl"
        return try {
            // Constructing the full class name and creating an instance of the factory.
            val fullClassName = if (fullPackage.isEmpty())
                implName
            else "$fullPackage.$implName"
            val aClass = Class.forName(fullClassName, true, klass.classLoader) as Class<T>
            aClass.getDeclaredConstructor().newInstance()
        } catch (e: ClassNotFoundException) {
            // Handling case where the implementation class cannot be found.
            throw RuntimeException(
                "Cannot find implementation for ${klass.canonicalName}. $implName does not " +
                        "exist"
            )
        } catch (e: IllegalAccessException) {
            // Handling case where the constructor is inaccessible.
            throw RuntimeException(
                "Cannot access the constructor ${klass.canonicalName}"
            )
        } catch (e: InstantiationException) {
            // Handling case where an instance cannot be created.
            throw RuntimeException(
                "Failed to create an instance of ${klass.canonicalName}"
            )
        }
    }
}