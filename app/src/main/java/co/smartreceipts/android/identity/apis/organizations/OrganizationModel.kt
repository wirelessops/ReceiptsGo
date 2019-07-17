package co.smartreceipts.android.identity.apis.organizations

data class OrganizationModel(
    val organization: Organization,
    val userRole: OrganizationUser.UserRole,
    val settingsMatch: Boolean
)