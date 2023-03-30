package io.ejekta.kambrikx.data

import io.ejekta.kambrik.Kambrik
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.io.File
import kotlin.properties.ReadWriteProperty

abstract class DataFile(val src: File) {

    internal data class DataRequest<T : Any>(val serializer: KSerializer<T>, val default: T) {
        val encoded: JsonElement
            get() {
                return Kambrik.Serial.Format.encodeToJsonElement(serializer, default)
            }

        fun encode(value: Any?): JsonElement {
            return Kambrik.Serial.Format.encodeToJsonElement(serializer, value as? T ?: default)
        }

        fun decode(data: JsonElement): T {
            return Kambrik.Serial.Format.decodeFromJsonElement(serializer, data)
        }
    }

    operator fun <T : Any> invoke(
        key: String,
        serializer: KSerializer<T>,
        default: () -> T
    ): ReadWriteProperty<Any, T> {
        return DataProperty(key, default, serializer, this)
    }

    inline fun <reified T : Any> of(
        key: String? = null,
        noinline default: () -> T
    ): ReadWriteProperty<Any, T> {
        return DataProperty(key, default, serializer(), this)
    }

    private val requests = mutableMapOf<String, DataRequest<*>>()

    private var results = mutableMapOf<String, JsonElement>()

    private val resultSerializer = MapSerializer(String.serializer(), JsonElement.serializer())

    private val loadedObjects = mutableMapOf<String, Any>()

    fun hasRequested(key: String) = key in requests

    open fun <T : Any> request(key: String, serializer: KSerializer<T>, default: T) {
        requests[key] = DataRequest(serializer, default)
        //println("Requested $key and serializer $serializer")
    }

    open fun load() {
        val file = src

        if (!file.exists()) {
            file.createNewFile()
            file.writeText(
                Kambrik.Serial.Format.encodeToString(resultSerializer, results)
            )
        }

        val contents = file.readText().ifBlank {
            "{}"
        }

        results = Kambrik.Serial.Format.decodeFromString(resultSerializer, contents).toMutableMap()

        for ((reqId, reqData) in requests) {
            val result = results.getOrPut(reqId) {
                reqData.encoded
            }
            val data = reqData.decode(result)
            loadedObjects[reqId] = data
        }
    }

    open fun save() {
        val file = src

        val outResults = mutableMapOf<String, JsonElement>()

        for ((objId, obj) in loadedObjects) {
            val data = requests[objId]!!
            outResults[objId] = data.encode(obj)
        }

        val output = Kambrik.Serial.Format.encodeToString(resultSerializer, outResults)

        file.apply {
            if (!exists()) {
                createNewFile()
            }
            writeText(output)
        }
    }

    // These are only called via the delegate properties, so casting is guaranteed to succeed

    internal open fun <R : Any> loadResult(key: String): R {
        return loadedObjects[key] as R
    }

    internal fun <R : Any> setResult(key: String, value: R) {
        loadedObjects[key] = value
    }

    companion object {
        fun forServer(file: File) = ConfigDataFile(file)
        fun forClient(file: File) = ServerDataFile(file)
    }
}