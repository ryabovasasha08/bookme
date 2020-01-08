package com.provectus_it.bookme.network.type_adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.lang.reflect.Type

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
        val jsonObject = json.asLong
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject), ZoneId.systemDefault())
    }

}