package com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.DaoMethod
import com.github.yuriisurzhykov.kevent.ksp.core.camelCase
import com.github.yuriisurzhykov.kevent.ksp.core.containsSuperType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

/**
 * The DaoKSClass represents a wrapper to generate DAO(Data Access Object) class. It provides methods and properties
 * to interact with the event class declaration and to get single-source data and methods for auto-generated DAOs.
 *
 * @property declaration The KSClassDeclaration representing the class for which the DAO is generated.
 * @property persistableWrapper The PersistableEntityKSClass representing the persistable entity wrapper for the DAO.
 * @property packageName The package name of the DAO class.
 * @property declaredClassName The string represents declared class name of the DAO.
 * @property actualClassName The string represents actual class name of the DAO.
 * @property className The [ClassName] representing the fully qualified class name of the generated DAO.
 * @property declarationName The [ClassName] representing the fully qualified class name of the event declaration.
 * @property mapperName The [ClassName] representing the fully qualified class name of the entity mapper.
 * @property entityName The [ClassName] representing the fully qualified class name of the DB entity.
 */
class DaoKSClass(
    private val declaration: KSClassDeclaration
) {

    private val persistableWrapper = PersistableEntityKSClass(declaration)
    val packageName = declaration.toClassName().packageName
    val declaredClassName = declaration.toClassName().simpleName
    val actualClassName = declaredClassName.plus("Dao")
    val className = ClassName(packageName, actualClassName)
    val declarationName = declaration.toClassName()
    val mapperName = persistableWrapper.mapperClassName
    val entityName = persistableWrapper.entityClassName

    fun getMethods(): List<DaoMethod> = listOf(
        DaoMethod.UPSERT,
        DaoMethod.GET_ALL,
        DaoMethod.GET_BY_KEY,
        DaoMethod.DELETE_BY_KEY,
        DaoMethod.DELETE,
        DaoMethod.CLEAR_TABLE
    )

    fun getPersistable(): PersistableEntityKSClass {
        return PersistableEntityKSClass(declaration)
    }

    fun getDaoMethodName(): String {
        return actualClassName.camelCase()
    }

    fun derivedFromComponent(clazz: KClass<*>): Boolean {
        return declaration.containsSuperType(clazz)
    }
}