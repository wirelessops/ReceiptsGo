package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrganizationUserKt(

    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "organization_id") val organizationId: String,
    @Json(name = "role") val role: Int, //todo 20.08.2018 server should get enum value (now Int)
    @Json(name = "created_at") val createdAt: String, //todo 18.08.18 Date adapter. format "2018-05-07T20:27:45.948Z"
    @Json(name = "updated_at") val updatedAt: String
) {
    enum class UserRole { //todo 05.10.18 maybe need to create special adapter with @FromJson and @ToJson
        ADMIN, SUPPORT_ADMIN, USER
    }

    /*ADMIN = 1
    SUPPORT_ADMIN = 5
    USER = 10*/
}

