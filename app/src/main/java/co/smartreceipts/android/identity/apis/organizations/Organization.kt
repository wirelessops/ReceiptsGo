package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Organization(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "created_at_iso8601") val createdAt: Date,
    @Json(name = "app_settings") val appSettings: AppSettings,
    @Json(name = "organization_users") val organizationUsers: List<OrganizationUser> = emptyList(),
    @Json(name = "error") val error: Error
)