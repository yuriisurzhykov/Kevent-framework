package com.github.yuriisurzhykov.kevent.events.persisted.core.database

import kotlin.reflect.KClass

/**
 * Annotation class used to define a database which contains both manually and auto created database
 * entities and DAOs.
 *
 * @param version           The version of the database.
 * @param exportSchema      Determines whether the schema should be exported in JSON for further auto migrations.
 * @param manualEntities    An optional array of database entities which are manually created by developer to include
 *                          in the generated database schema.
 * @param autoMigrations    An optional array of [AutoMigration] to apply to the database.
 */
annotation class AutoDatabase(
    val version: Int,
    val exportSchema: Boolean,
    val manualEntities: Array<KClass<out Any>> = [],
    val autoMigrations: Array<AutoMigration> = []
)