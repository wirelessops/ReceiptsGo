package co.smartreceipts.android.identity.apis.organizations.adapters

import co.smartreceipts.android.model.impl.ImmutableCategoryImpl
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class CategoryJson(
    @Json(name = "uuid") val id: String, //todo 20.08.18 server id: String, app Category id: Int
    @Json(name = "Name") val name: String,
    @Json(name = "Code") val code: String
)

class CategoryJsonAdapter {
    //todo 5.10.18 Category isn't an interface now. Check after rebase
    // Note: Maybe later we'll be able to use Category interface for this (see https://github.com/square/moshi/issues/133)

    @FromJson fun categoryFromJson(categoryJson: CategoryJson) = ImmutableCategoryImpl(-1, categoryJson.name, categoryJson.code)

    @ToJson fun categoryToJson(category: ImmutableCategoryImpl) = CategoryJson("", category.name, category.code)
}

/*
* "uuid": "b00ffd0d-ba7d-4285-a2ee-c4c8930cc486",
            "Name": "Name1",
            "Code": "Code1"
* */