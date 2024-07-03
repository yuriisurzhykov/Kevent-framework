package com.github.yuriisurzhykov.kevent.activeobject.manager.events

import com.github.yuriisurzhykov.kevent.activeobject.common.ActiveObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 *  Kotlin Serializer for class [ClassSerialWrapper].
 *  It serializes class as a string with qualified class name, and deserializes using that string
 *  trying to create instance of class description using qualified class name.
 *  If class cannot be created by string or cannot be found it provides `ActiveObject::class`
 * */
object ClassWrapperSerializer : KSerializer<ClassSerialWrapper> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("KClass", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ClassSerialWrapper {
        return try {
            ClassSerialWrapper(Class.forName(decoder.decodeString()).kotlin)
        } catch (e: Exception) {
            // Because of this class is used only for ActiveObject class references
            // we pass ActiveObject class if there was a failure
            ClassSerialWrapper(ActiveObject::class)
        }
    }

    override fun serialize(encoder: Encoder, value: ClassSerialWrapper) {
        encoder.encodeString(value.clazz.qualifiedName.toString())
    }
}
