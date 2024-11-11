package com.wops.receiptsgo.apis.moshi.adapters

import com.wops.receiptsgo.identity.apis.organizations.OrganizationUser
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class UserRoleJsonAdapter {

    @FromJson
    fun userRoleFromJson(intRole: Int): OrganizationUser.UserRole = OrganizationUser.UserRole.getFromIntValue(intRole)

    @ToJson
    fun userRoleToJson(userRole: OrganizationUser.UserRole): String = userRole.intValue.toString()
}