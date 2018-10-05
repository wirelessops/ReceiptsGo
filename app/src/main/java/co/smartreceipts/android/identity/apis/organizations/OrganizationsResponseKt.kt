package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class OrganizationsResponseKt(
    @Json(name = "organizations") val organizations: List<OrganizationKt> = emptyList()
) : Serializable
