package co.smartreceipts.android.apis.moshi.adapters

import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationSettings
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.json.JSONObject

class OrganizationSettingsJsonAdapter {

    @FromJson
    fun settingsFromJson(map: Map<String, String>): OrganizationSettings = OrganizationSettings(JSONObject(map))

    @ToJson
    fun settingsToJson(organizationSettings: OrganizationSettings): String = organizationSettings.jsonObject.toString()
}