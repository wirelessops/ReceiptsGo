package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.model.Category
import co.smartreceipts.android.model.Column
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.model.Receipt
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class AppSettings(
    @Json(name = "Configurations") val configurations: Configurations,
    @Json(name = "Settings") val settings: OrganizationSettings,
    @Json(name = "Categories") val categories: List<Category>,
    @Json(name = "PaymentMethods") val paymentMethods: List<PaymentMethod>,
    @Json(name = "CSVColumns") val csvColumns: List<Column<Receipt>>,
    @Json(name = "PDFColumns") val pdfColumns: List<Column<Receipt>>
) {

    data class OrganizationSettings(val jsonObject: JSONObject)
}
