package com.wops.receiptsgo.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class OrganizationsResponse(
    @Json(name = "organizations") val organizations: List<Organization> = emptyList()
) : Serializable
