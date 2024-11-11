package com.wops.receiptsgo.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Error(
    @Json(name = "has_error") val hasError: Boolean = false,
    @Json(name = "errors") val errors: List<String> = emptyList()
)

