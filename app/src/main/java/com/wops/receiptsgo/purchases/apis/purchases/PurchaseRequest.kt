package com.wops.receiptsgo.purchases.apis.purchases

import com.wops.receiptsgo.purchases.model.ManagedProduct

class PurchaseRequest(managedProduct: ManagedProduct) {
    private val signature: String = managedProduct.inAppDataSignature
    private val receipt: String = managedProduct.purchaseDataJson
    private val pay_service: String = "Google Play"
}