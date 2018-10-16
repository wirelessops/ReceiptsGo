package co.smartreceipts.android.apis.moshi.adapters

import co.smartreceipts.android.model.Category
import co.smartreceipts.android.model.factory.CategoryBuilderFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.util.*


class CategoryJsonAdapter {
    @FromJson
    fun categoryFromJson(categoryJson: CategoryJson) =
        CategoryBuilderFactory().setUuid(UUID.fromString(categoryJson.uuid))
            .setName(categoryJson.name)
            .setCode(categoryJson.code)
            .build()

    @ToJson
    fun categoryToJson(category: Category) = CategoryJson(category.uuid.toString(), category.name, category.code)


    @JsonClass(generateAdapter = true)
    data class CategoryJson(
        @Json(name = "uuid") val uuid: String,
        @Json(name = "Name") val name: String,
        @Json(name = "Code") val code: String
    )
}