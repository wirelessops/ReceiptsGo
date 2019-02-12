package co.smartreceipts.android.purchases.model

import com.google.gson.annotations.SerializedName

data class AvailablePurchase(

    @SerializedName("productId")
    private val productId: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("price_amount_micros")
    val priceAmountMicros: Long = 0,

    @SerializedName("price_currency_code")
    val priceCurrencyCode: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("description")
    val description: String? = null
) {

    fun getInAppPurchase(): InAppPurchase? = InAppPurchase.from(productId)
}
