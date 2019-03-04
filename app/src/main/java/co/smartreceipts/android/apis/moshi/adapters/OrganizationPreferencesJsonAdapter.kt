package co.smartreceipts.android.apis.moshi.adapters

import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationPreferences
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.json.JSONObject


class OrganizationPreferencesJsonAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): OrganizationPreferences = OrganizationPreferences(JSONObject(reader.readJsonValue() as Map<String, Any>))

    @ToJson
    fun settingsToJson(organizationPreferences: OrganizationPreferences): String = organizationPreferences.preferencesJson.toString()
}