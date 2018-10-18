package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.model.Category
import co.smartreceipts.android.model.PaymentMethod
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class AppSettings(

    //todo 12.10.18 add column serialization
    //    @SerializedName("CSVColumns")
    //    private List<Column<Receipt>> csvColumns;
    //    @SerializedName("PDFColumns")
    //    private List<Column<Receipt>> pdfColumns;

    @Json(name = "Configurations") val configurations: Configurations,
    @Json(name = "Settings") val settings: OrganizationSettings,
    @Json(name = "Categories") val categories: List<Category>,
    @Json(name = "PaymentMethods") val paymentMethods: List<PaymentMethod>
)

{
    data class OrganizationSettings(val jsonObject: JSONObject)
}
