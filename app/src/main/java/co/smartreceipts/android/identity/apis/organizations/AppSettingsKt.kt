package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.model.Category
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AppSettingsKt(

    // TODO: 14.08.2018 Unable to invoke no-args constructor for interface co.smartreceipts.android.model.Category. Register an InstanceCreator with Gson for this type may fix this problem.
    //    @SerializedName("Categories")
    //    private List<Category> categories;
    //    @SerializedName("PaymentMethods")
    //    private List<PaymentMethod> paymentMethods;
    //    @SerializedName("CSVColumns")
    //    private List<Column<Receipt>> csvColumns;
    //    @SerializedName("PDFColumns")
    //    private List<Column<Receipt>> pdfColumns;

    @Json(name = "Configurations") val configurations: ConfigurationsKt,
    @Json(name = "Settings") val settings: OrganizationSettingsKt,
    @Json(name = "Categories") val categories: List<Category>
)
