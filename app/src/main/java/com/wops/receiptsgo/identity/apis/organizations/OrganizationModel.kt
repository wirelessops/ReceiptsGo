package com.wops.receiptsgo.identity.apis.organizations

data class OrganizationModel(
    val organization: Organization,
    val userRole: OrganizationUser.UserRole,
    val settingsMatch: Boolean
)