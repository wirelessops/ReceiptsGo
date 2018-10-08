package co.smartreceipts.android.identity.apis.organizations.adapters

import co.smartreceipts.android.model.Category
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.util.*

@JsonClass(generateAdapter = true)
data class CategoryJson(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Code") val code: String
)

class CategoryJsonAdapter {
    @FromJson fun categoryFromJson(categoryJson: CategoryJson) = Category(-1, UUID.fromString(categoryJson.uuid), categoryJson.name, categoryJson.code)

    @ToJson fun categoryToJson(category: Category) = CategoryJson("", category.name, category.code)
}

/*
* "uuid": "b00ffd0d-ba7d-4285-a2ee-c4c8930cc486",
            "Name": "Name1",
            "Code": "Code1"
* */