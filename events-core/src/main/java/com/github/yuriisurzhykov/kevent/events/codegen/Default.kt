package com.github.yuriisurzhykov.kevent.events.codegen

/**
 * The annotation is used to provide a way to set default value for any parameter for
 * sticky events(including sticky collection).
 * Default annotation can only  be used with the following type:
 * - All primitive types (byte, short, int, long, char, float, double, boolean)
 * - All primitive arrays (BooleanArray, ByteArray, ShortArray, IntArray, LongArray)
 * - Strings
 * - Enums
 * - Arrays of enums
 * - Arrays of primitives (Array<Int>, Array<String>, Array<Boolean>, etc.)
 * - Collections of primitive types (Set<Int>, List<Short>, MutableList<Int>, etc.)
 * - Collections of enums (Set<Enum type>, List<Enum type>)
 *
 * This annotations is used for 2 purposes:
 *  1. Generate code to create a default instance of the sticky event. If the @Default annotation
 *  provided for the property that one will be used to create the default instance. Otherwise
 *  the syntax defaults will be used.
 *  2. Generate default value for persistable logic. If the @Default annotation provided for the
 *  property that one will be used for persistence default. Persistence default is used mostly in
 *  migration purposes, when you upgraded the version of database and added new field (-s) to the
 *  event, and there is no value in rows for that field, so the default value will be used.
 *
 * */
@Retention(AnnotationRetention.BINARY)
annotation class Default(val value: String)