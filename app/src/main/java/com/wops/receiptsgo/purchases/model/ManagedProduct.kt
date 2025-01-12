package com.wops.receiptsgo.purchases.model

interface ManagedProduct {
    /**
     * @return the [InAppPurchase] enum that keys this managed product type
     */
    val inAppPurchase: InAppPurchase

    /**
     * @return the purchase data json [String] provided by Google for this purchase
     */
    val purchaseDataJson: String

    /**
     * @return the [String] purchase token provided by Google for this product
     */
    val purchaseToken: String

    /**
     * @return the [String] data signature provided by Google for this product
     */
    val inAppDataSignature: String
}