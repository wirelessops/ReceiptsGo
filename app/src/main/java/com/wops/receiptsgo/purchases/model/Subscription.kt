package com.wops.receiptsgo.purchases.model

data class Subscription(
    override val inAppPurchase: InAppPurchase,
    override val purchaseDataJson: String,
    override val purchaseToken: String,
    override val inAppDataSignature: String
): ManagedProduct