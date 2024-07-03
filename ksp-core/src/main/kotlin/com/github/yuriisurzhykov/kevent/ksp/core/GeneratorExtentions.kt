package com.github.yuriisurzhykov.kevent.ksp.core

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KSTypesNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
fun parseAnnotationClassParameterList(block: () -> List<KClass<*>>): List<ClassName> {
    return try {
        block.invoke().map { ClassName(it.java.`package`.name, it.simpleName.orEmpty()) }
    } catch (e: KSTypesNotPresentException) {
        /**
         * Bug: ksp: com.google.devtools.ksp.KSTypesNotPresentException: java.lang.ClassNotFoundException:
         * Official document: https://github.com/google/ksp/issues?q=ClassNotFoundException++KClass%3C*%3E
         * temporary fix method as follows, but it is not perfect!!!
         * TODO completely fix it!
         * */
        val res = mutableListOf<ClassName>()
        val ksTypes = e.ksTypes
        for (ksType in ksTypes) {
            val declaration = ksType.declaration
            if (declaration is KSClassDeclaration) {
                res.add(declaration.toClassName())
            }
        }
        res
    }
}

@OptIn(KspExperimental::class)
fun parseAnnotationClassParameter(block: () -> KClass<*>): ClassName {
    return try {
        val clazz = block.invoke()
        ClassName(clazz.java.`package`.name, clazz.simpleName.orEmpty())
    } catch (e: KSTypeNotPresentException) {
        /**
         * Bug: ksp: com.google.devtools.ksp.KSTypeNotPresentException: java.lang.ClassNotFoundException: cn.jailedbird.arouter.ksp.test.TestInterface
         * Official document: https://github.com/google/ksp/issues?q=ClassNotFoundException++KClass%3C*%3E
         * temporary fix method as follows, but it is not perfect!!!
         * TODO completely fix it!
         * */
        e.ksType.toClassName()
    }
}