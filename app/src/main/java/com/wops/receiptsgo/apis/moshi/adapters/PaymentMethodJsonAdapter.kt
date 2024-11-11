package com.wops.receiptsgo.apis.moshi.adapters

import com.wops.receiptsgo.model.PaymentMethod
import com.wops.receiptsgo.model.factory.PaymentMethodBuilderFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.util.*

class PaymentMethodJsonAdapter {
    @FromJson
    fun paymentMethodFromJson(methodJson: PaymentMethodJson) =
        PaymentMethodBuilderFactory().setUuid(UUID.fromString(methodJson.uuid))
        .setMethod(methodJson.code)
        .build()

    @ToJson
    fun paymentMethodToJson(method: PaymentMethod) = PaymentMethodJson(method.uuid.toString(), method.method)


    @JsonClass(generateAdapter = true)
    data class PaymentMethodJson(
        @Json(name = "uuid") val uuid: String,
        @Json(name = "Code") val code: String
    )
}