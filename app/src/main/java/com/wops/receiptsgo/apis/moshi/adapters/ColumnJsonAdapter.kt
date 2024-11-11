package com.wops.receiptsgo.apis.moshi.adapters

import com.wops.receiptsgo.model.Column
import com.wops.receiptsgo.model.ColumnDefinitions
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.ColumnBuilderFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.util.*

class ColumnJsonAdapter(private val definitions: ColumnDefinitions<Receipt>) {

    @FromJson
    fun columnFromJson(columnJson: ColumnJson) : Column<Receipt> = ColumnBuilderFactory(definitions)
        .setColumnUuid(UUID.fromString(columnJson.uuid))
        .setColumnType(columnJson.columnType)
        .build()

    @ToJson
    fun columnToJson(column: Column<Receipt>) = ColumnJson(column.uuid.toString(), column.type)

    @JsonClass(generateAdapter = true)
    data class ColumnJson(
        @Json(name = "uuid") val uuid: String,
        @Json(name = "column_type") val columnType: Int
    )
}