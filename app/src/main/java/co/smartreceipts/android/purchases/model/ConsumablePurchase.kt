package co.smartreceipts.android.purchases.model

data class ConsumablePurchase(
    override val inAppPurchase: InAppPurchase,
    override val purchaseToken: String,
    override val purchaseData: String,
    override val inAppDataSignature: String
) : ManagedProduct