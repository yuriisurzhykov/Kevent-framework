package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.github.yuriisurzhykov.kevent.events.persisted.core.database.AutoDatabase
import com.github.yuriisurzhykov.kevent.ksp.core.IMPL_NAME
import com.github.yuriisurzhykov.kevent.ksp.core.parseAnnotationClassParameterList
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Represents a class that interacts with an annotated [AutoDatabase] database class.
 * It gives more simple and single-way access to all necessary properties to generate database class.
 *
 * @property packageName The package name of the annotated database class.
 * @property actualClassName The actual class name of the annotated database class.
 * @property className The ClassName representing the annotated database class.
 * @property declaredClass The ClassName representing the declared class of the annotated database class.
 * @property annotation The AutoDatabase annotation applied to the annotated database class.
 * @property databaseVersion The version of the database specified in the AutoDatabase annotation.
 * @property exportSchema Determines whether the schema should be exported in JSON for further auto migrations.
 * @property migrations The list of AutoMigrations to apply to the database.
 * @property optionalEntities The list of optional entities manually created by the developer to include in the
 * generated database schema.
 */
class AutoDatabaseKSClass(
    declaration: KSClassDeclaration
) {

    val packageName = declaration.toClassName().packageName
    val actualClassName = declaration.toClassName().simpleName.plus(IMPL_NAME)
    val className = ClassName(packageName, actualClassName)
    val declaredClass = declaration.toClassName()

    @OptIn(KspExperimental::class)
    val annotation = declaration.getAnnotationsByType(AutoDatabase::class).first()
    val databaseVersion = annotation.version
    val exportSchema = annotation.exportSchema
    val migrations = annotation.autoMigrations.toList()
    val optionalEntities = parseAnnotationClassParameterList { annotation.manualEntities.asList() }
}