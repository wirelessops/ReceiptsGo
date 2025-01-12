package com.wops.receiptsgo.apis.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException

import java.lang.reflect.Type
import java.text.ParseException
import java.util.Date

import com.wops.receiptsgo.date.Iso8601DateFormat
import com.google.gson.JsonPrimitive

class GsonUtcDateAdapter : GsonAdapter<Date> {

    private val iso8601DateFormat = Iso8601DateFormat()

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        if (json is JsonPrimitive) {
            if (json.isString) {
                // Our new service endpoints should return iso 8601 strings for dates
                try {
                    return iso8601DateFormat.parse(json.asString)
                } catch (e: ParseException) {
                    throw JsonParseException(e)
                }
            } else if (json.isNumber) {
                // For legacy reasons, our endpoint can also return a long for the date
                return Date(json.asLong)
            }
        }
        throw JsonParseException("Failed to parse: $json")
    }
}
