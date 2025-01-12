package com.wops.receiptsgo.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class OrganizationUser(

    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "organization_id") val organizationId: String,
    @Json(name = "role") val role: UserRole,
    @Json(name = "created_at_iso8601") val createdAt: Date,
    @Json(name = "updated_at_iso8601") val updatedAt: Date
) {
    enum class UserRole(val intValue: Int) {
        ADMIN(1), SUPPORT_ADMIN(5), USER(10);

        companion object {
            fun getFromIntValue(value: Int): UserRole = values().find { value == it.intValue } ?: throw IllegalStateException("Unsupported UserRole int value")
        }
    }
}

