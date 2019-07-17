package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppSettingsPutWrapper(@Json(name = "app_settings") val appSettings: AppSettings)