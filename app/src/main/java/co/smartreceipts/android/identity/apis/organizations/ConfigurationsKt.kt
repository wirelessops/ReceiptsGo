package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfigurationsKt(
    @Json(name = "IsSettingsEnable") val isSettingsEnabled: Boolean? = false  //todo 20.08.18: nullable?
)
