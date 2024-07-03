package com.github.yuriisurzhykov.kevent.activeobject.manager.events

import kotlin.reflect.KClass

/**
 *  This is stub class for kotlin serialization. Because it is not able to serialize and deserialize
 *  KClass objects we have to create this stub class as a wrapper/holder for class value and use
 *  custom serializer [ClassWrapperSerializer].
 * */
data class ClassSerialWrapper(val clazz: KClass<*>)