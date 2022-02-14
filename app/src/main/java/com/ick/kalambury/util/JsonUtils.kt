package com.ick.kalambury.util

import android.util.Base64
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object JsonUtils {

    @JvmStatic
    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    fun toJson(any: Any): String = objectMapper.writeValueAsString(any)

    fun toJson(stream: OutputStream, any: Any) = objectMapper.writeValue(stream, any)

    fun <T> fromJson(json: ByteArray, clazz: Class<T>): T = objectMapper.readValue(json, clazz)

    @Throws(IOException::class)
    fun <T> fromJson(json: String, clazz: Class<T>): T = objectMapper.readValue(json, clazz)

    fun <T> fromJson(json: InputStream, clazz: Class<T>): T = objectMapper.readValue(json, clazz)

}

class ByteArraySerializer : JsonSerializer<ByteArray>() {

    override fun serialize(value: ByteArray?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen!!.writeString(Base64.encodeToString(value, Base64.NO_WRAP or Base64.NO_PADDING))
    }

}

class ByteArrayDeserializer : JsonDeserializer<ByteArray>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ByteArray {
        return Base64.decode(p!!.valueAsString, Base64.NO_WRAP or Base64.NO_PADDING)
    }

}