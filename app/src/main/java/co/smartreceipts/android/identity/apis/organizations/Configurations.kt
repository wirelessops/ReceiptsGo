package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Configurations(
    @Json(name = "IsSettingsEnable") val isSettingsEnabled: Boolean? = false
)
