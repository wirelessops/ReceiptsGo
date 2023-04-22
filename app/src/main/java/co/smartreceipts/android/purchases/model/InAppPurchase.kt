package co.smartreceipts.android.purchases.model

import androidx.annotation.VisibleForTesting
import com.android.billingclient.api.BillingClient
import java.util.*

enum class InAppPurchase(
    /**
     * the type of {@link ManagedProduct} that this is
     */
    val type: Class<out ManagedProduct>,
    val sku: String,
    /**
     * For subscriptions, Google does not allow for price changes to occur. To better handle this,
     * we allow "legacy" skus (ie stock keeping unit) to still properly map to our purchase type.
     * Callers of this method will receive a full set of legacy skus that correspond to historical
     * pricing paradigms
     * @return a [Set] of [String] identifiers for this product.
     **/
    val legacySkus: Set<String>,
    /**
     * the [Set] of all [PurchaseFamily] that are supported for this purchase type
     */
    val purchaseFamilies: Set<PurchaseFamily>
) {
    // Note: Smart Receipts Plus users also get some free OCR scans
    SmartReceiptsPlus(
        Subscription::class.java, "plus_sku_4", hashSetOf("pro_sku_3", "plus_sku_5"),
        hashSetOf(PurchaseFamily.SmartReceiptsPlus, PurchaseFamily.Ocr)
    ),
    OcrScans10(ConsumablePurchase::class.java, "ocr_purchase_10", PurchaseFamily.Ocr),
    OcrScans50(ConsumablePurchase::class.java, "ocr_purchase_1", PurchaseFamily.Ocr),

    StandardSubscriptionPlan(Subscription::class.java, "and_autorec_1month", PurchaseFamily.SubscriptionPlans),
    PremiumSubscriptionPlan(Subscription::class.java, "and_autorec_pro_1month", PurchaseFamily.SubscriptionPlans),

    /**
     * A test only [ConsumablePurchase] for testing without a particular [PurchaseFamily]
     */
    @VisibleForTesting
    TestConsumablePurchase(ConsumablePurchase::class.java, "test_consumable_purchase", emptySet()),

    /**
     * A test only [Subscription] for testing without a particular [PurchaseFamily]
     */
    @VisibleForTesting
    TestSubscription(
        Subscription::class.java,
        "test_subscription",
        setOf("test_legacy_subscription"),
        emptySet()
    );

    constructor(
        type: Class<out ManagedProduct?>,
        sku: String,
        purchaseFamily: PurchaseFamily
    ) : this(type, sku, setOf(purchaseFamily))

    constructor(
        type: Class<out ManagedProduct?>,
        sku: String,
        purchaseFamilies: Set<PurchaseFamily>
    ) : this(type, sku, emptySet<String>(), purchaseFamilies)

    /**
     * @return the [String] of the Google product type (ie "inapp" or "subs")
     */
    val productType: String
        get() = when (type) {
            ConsumablePurchase::class.java -> BillingClient.SkuType.INAPP
            else -> BillingClient.SkuType.SUBS
        }

    companion object {
        @JvmStatic
        fun from(sku: String?): InAppPurchase? {
            for (inAppPurchase in values()) {
                if (inAppPurchase.sku == sku) {
                    return inAppPurchase
                }
                for (legacySku in inAppPurchase.legacySkus) {
                    if (legacySku == sku) {
                        return inAppPurchase
                    }
                }
            }
            return null
        }

        @JvmStatic
        val consumablePurchaseSkus: ArrayList<String>
            get() {
                val skus = ArrayList<String>(values().size)
                for (inAppPurchase in values()) {
                    if (ConsumablePurchase::class.java == inAppPurchase.type && inAppPurchase != TestConsumablePurchase) {
                        skus.add(inAppPurchase.sku)
                    }
                }
                return skus
            }

        @JvmStatic
        val subscriptionSkus: ArrayList<String>
            get() {
                val skus = ArrayList<String>(values().size)
                for (inAppPurchase in values()) {
                    if (Subscription::class.java == inAppPurchase.type && inAppPurchase != TestSubscription) {
                        skus.add(inAppPurchase.sku)
                    }
                }
                return skus
            }
    }
}