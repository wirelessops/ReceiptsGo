package com.wops.receiptsgo.identity.apis.organizations

import com.wops.receiptsgo.model.Category
import com.wops.receiptsgo.model.Column
import com.wops.receiptsgo.model.PaymentMethod
import com.wops.receiptsgo.model.Receipt
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppSettings(
    @Json(name = "Configurations") val configurations: Configurations,
    @Json(name = "Settings") val preferences: Map<String, Any?>,
    @Json(name = "Categories") val categories: List<Category>,
    @Json(name = "PaymentMethods") val paymentMethods: List<PaymentMethod>,
    @Json(name = "CSVColumns") val csvColumns: List<Column<Receipt>>,
    @Json(name = "PDFColumns") val pdfColumns: List<Column<Receipt>>
)
