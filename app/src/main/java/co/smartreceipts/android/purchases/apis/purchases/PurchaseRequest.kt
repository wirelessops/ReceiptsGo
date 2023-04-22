package co.smartreceipts.android.purchases.apis.purchases

import co.smartreceipts.android.purchases.model.ManagedProduct

class PurchaseRequest(managedProduct: ManagedProduct) {
    private val signature: String = managedProduct.inAppDataSignature
    private val receipt: String = managedProduct.purchaseDataJson
    private val pay_service: String = "Google Play"
}