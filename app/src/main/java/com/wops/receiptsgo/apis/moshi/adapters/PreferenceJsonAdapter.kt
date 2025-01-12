package com.wops.receiptsgo.apis.moshi.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

/**
 * This adapter works with different preference types: String, Int, Boolean, Float
 */
class PreferenceJsonAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Any? {

        return if (reader.peek() == JsonReader.Token.NUMBER) {
            val numberAsString = reader.nextString()
            numberAsString.toIntOrNull() ?: numberAsString.toFloatOrNull()
        } else { //
            reader.readJsonValue()
        }

    }
}