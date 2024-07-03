package com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators

import androidx.room.Dao
import com.github.yuriisurzhykov.kevent.events.persisted.core.dao.AutoDao
import com.github.yuriisurzhykov.kevent.events.persisted.ksp.wrappers.DaoKSClass
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * The `DaoInterfaceGenerator` class is responsible for generating a DAO (Data Access Object) Kotlin interface
 * based on the provided entity class wrapper. Entity class wrapper is an easy-access .
 *
 * @property entity The `DaoKSClass` representing the entity for which the DAO is generated.
 */
class DaoInterfaceGenerator(
    private val entity: DaoKSClass
) : com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.FileGenerator {

    override fun generate(): FileSpec {
        return FileSpec
            //Generate file
            .builder(entity.packageName, entity.actualClassName)
            // Generate interface
            .addType(TypeSpec.interfaceBuilder(entity.actualClassName)
                // Add @Dao annotation
                .addAnnotation(Dao::class)
                // Add @AutoDao annotation to be able to find it for AutoDatabase
                .addAnnotation(AutoDao::class)
                .apply {
                    entity.getMethods().forEach { method ->
                        // Generate methods defined for entity
                        addFunction(
                            com.github.yuriisurzhykov.kevent.events.persisted.ksp.generators.DaoMethodGenerator(
                                method
                            ).generateFor(entity.getPersistable(), method)
                        )
                    }
                }
                .build()
            )
            .build()
    }
}