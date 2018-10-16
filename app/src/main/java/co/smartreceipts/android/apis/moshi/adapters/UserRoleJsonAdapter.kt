package co.smartreceipts.android.apis.moshi.adapters

import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class UserRoleJsonAdapter {

    @FromJson
    fun userRoleFromJson(intRole: Int): OrganizationUser.UserRole = OrganizationUser.UserRole.getFromIntValue(intRole)

    @ToJson
    fun userRoleToJson(userRole: OrganizationUser.UserRole): String = userRole.intValue.toString()
}