package com.wops.receiptsgo.purchases.apis.purchases

import java.util.*

data class PurchaseResponse(
    val status: String?,
    val errors: List<String>?,
    val purchase: MobileAppPurchase?
) {

    class MobileAppPurchase constructor(
        val id: String?,
        val user_id: String?,
        val pay_service: String?,
        val purchase_id: String?,
        val product_id: String?,
        val package_name: String?,
        val purchase_time: Date?,
        val subscription_active: Boolean?,
        val created_at: Date?,
        val updated_at: Date?
    )
}