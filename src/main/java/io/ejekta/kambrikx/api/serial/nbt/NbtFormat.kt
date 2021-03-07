package io.ejekta.kambrikx.api.serial.nbt

import io.ejekta.kambrik.Kambrik
import io.ejekta.kambrikx.internal.serial.decoders.TagMapDecoder
import io.ejekta.kambrikx.internal.serial.encoders.TagEncoder
import io.ejekta.kambrikx.internal.serial.encoders.TaglessEncoder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

class NbtFormatConfig {

    private val nbtEncodingMarker = Kambrik.Logging.createMarker("NBT-SERIAL")

    private val logger = Kambrik.Logger

    internal fun logInfo(level: Int, msg: String) {
        logger.info(nbtEncodingMarker, "\t".repeat(level) + msg)
    }

    var classDiscriminator: String = "type"

    @ExperimentalSerializationApi
    var serializersModule: SerializersModule = EmptySerializersModule

    var writePolymorphic = true
}

@InternalSerializationApi
@ExperimentalSerializationApi
open class NbtFormat internal constructor(val config: NbtFormatConfig) : SerialFormat {

    override val serializersModule = EmptySerializersModule + config.serializersModule

    companion object Default : NbtFormat(NbtFormatConfig())

    @InternalSerializationApi
    fun <T> encodeToTag(serializer: SerializationStrategy<T>, obj: T): Tag {
        return when (serializer.descriptor.kind) {
            is PrimitiveKind -> {
                val enc = TaglessEncoder(config, 0)
                enc.encodeSerializableValue(serializer, obj)
                enc.root
            }
            else -> {
                val enc = TagEncoder(config)
                enc.encodeSerializableValue(serializer, obj)
                enc.root
            }
        }
    }

    @ExperimentalSerializationApi
    inline fun <reified T> encodeToTag(obj: T) = encodeToTag(EmptySerializersModule.serializer(), obj)

    fun <T> decodeFromTag(deserializer: DeserializationStrategy<T>, obj: Tag): T {
        val decoder = when (obj) {
            is CompoundTag -> TagMapDecoder(config, 0, obj)
            else -> throw Exception("Cannot decode this!")
        }
        return decoder.decodeSerializableValue(deserializer)
    }

}

@InternalSerializationApi
@ExperimentalSerializationApi
fun NbtFormat(config: NbtFormatConfig.() -> Unit): NbtFormat {
    return NbtFormat(NbtFormatConfig().apply(config))
}


