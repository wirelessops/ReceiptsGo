package co.smartreceipts.android.identity.apis.organizations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrganizationSettingsKt(

    //TODO 20.08.18 shouldn't ne nonnull?

    @Json(name = "TripDuration") val tripDuration: Int? = 0,
    @Json(name = "isocurr") val currencyCode: String? = "",
    @Json(name = "dateseparator") val dateSeparator: String? = "",
    @Json(name = "trackcostcenter") val trackCostCenter: Boolean? = false, // todo 18.08.18: try to use default values from pref_general_track_cost_center_defaultValue
    @Json(name = "PredictCats") val predictCategory: Boolean? = true,
    @Json(name = "MatchNameCats") val matchNameToCategory: Boolean? = false,
    @Json(name = "MatchCommentCats") val matchCommentToCategory: Boolean? = false,
    @Json(name = "OnlyIncludeExpensable") val onlyIncludeExpensable: Boolean? = false,
    @Json(name = "ExpensableDefault") val expensableDefault: Boolean? = false,
    @Json(name = "IncludeTaxField") val includeTaxField: Boolean? = false,
    @Json(name = "TaxPercentage") val taxPercentage: Float? = 0f,
    @Json(name = "PreTax") val preTax: Boolean? = false,
    @Json(name = "EnableAutoCompleteSuggestions") val enableAutoCompleteSuggestions: Boolean? = false,
    @Json(name = "MinReceiptPrice") val minReceiptPrice: Float? = 0f,
    @Json(name = "DefaultToFirstReportDate") val defaultToFirstReportDate: Boolean? = false,
    @Json(name = "ShowReceiptID") val showReceiptID: Boolean? = false,
    @Json(name = "UseFullPage") val useFullPage: Boolean? = false,
    @Json(name = "UsePaymentMethods") val usePaymentMethods: Boolean? = true,
    @Json(name = "IncludeCSVHeaders") val includeCSVHeaders: Boolean? = false,
    @Json(name = "PrintByIDPhotoKey") val printByIDPhotoKey: Boolean? = false,
    @Json(name = "PrintCommentByPhoto") val printCommentByPhoto: Boolean? = false,
    @Json(name = "EmailTo") val emailTo: String? = "",
    @Json(name = "EmailCC") val emailCC: String? = "",
    @Json(name = "EmailBCC") val emailBCC: String? = "",
    @Json(name = "EmailSubject") val emailSubject: String? = "",
    @Json(name = "SaveBW") val saveBW: Boolean? = false,
    @Json(name = "LayoutIncludeReceiptDate") val layoutIncludeReceiptDate: Boolean? = true,
    @Json(name = "LayoutIncludeReceiptCategory") val layoutIncludeReceiptCategory: Boolean? = true,
    @Json(name = "LayoutIncludeReceiptPicture") val layoutIncludeReceiptPicture: Boolean? = true,
    @Json(name = "MileageTotalInReport") val mileageTotalInReport: Boolean? = false,
    @Json(name = "MileageRate") val mileageRate: Float? = 0f,
    @Json(name = "MileagePrintTable") val mileagePrintTable: Boolean? = false,
    @Json(name = "MileageAddToPDF") val mileageAddToPDF: Boolean? = false,
    @Json(name = "PdfFooterString") val pdfFooterString: String? = ""
)
