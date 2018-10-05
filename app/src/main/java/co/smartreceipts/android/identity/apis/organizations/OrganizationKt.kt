package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class OrganizationKt(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "created_at") val createdAt: Long = 0, //todo 22.08.18 different date formatting
    @Json(name = "app_settings") val appSettings: AppSettingsKt,
    @Json(name = "organization_users") val organizationUsers: List<OrganizationUserKt> = emptyList(),
    @Json(name = "error") val error: ErrorKt
) : Serializable