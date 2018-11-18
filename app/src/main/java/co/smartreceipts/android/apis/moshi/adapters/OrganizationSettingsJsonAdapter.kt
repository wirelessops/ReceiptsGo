package co.smartreceipts.android.apis.moshi.adapters

import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationSettings
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.json.JSONObject


class OrganizationSettingsJsonAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): OrganizationSettings = OrganizationSettings(JSONObject(reader.readJsonValue() as Map<String, Any>))

    @ToJson
    fun settingsToJson(organizationSettings: OrganizationSettings): String = organizationSettings.jsonObject.toString()
}