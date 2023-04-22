package co.smartreceipts.android.purchases.consumption

import android.content.SharedPreferences
import co.smartreceipts.android.purchases.model.InAppPurchase.Companion.from
import co.smartreceipts.android.purchases.model.PurchaseFamily
import co.smartreceipts.android.purchases.model.Subscription
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.common.base.Preconditions
import dagger.Lazy
import io.reactivex.Completable
import java.util.*
import javax.inject.Inject

@ApplicationScope
class SubscriptionInAppPurchaseConsumer @Inject constructor(preferences: Lazy<SharedPreferences>) :
    InAppPurchaseConsumer<Subscription> {

    private val sharedPreferences: Lazy<SharedPreferences>

    companion object {
        private const val KEY_CONSUMED_SUBSCRIPTION_SET = "key_consumed_subscription_set"
        private const val FORMAT_KEY_PURCHASE_FAMILY = "key_%s_purchase_family"
    }

    init {
        sharedPreferences = Preconditions.checkNotNull(preferences)
    }

    override fun isConsumed(managedProduct: Subscription, purchaseFamily: PurchaseFamily): Boolean {
        val consumedSubscriptionSkuSet = sharedPreferences.get().getStringSet(
            KEY_CONSUMED_SUBSCRIPTION_SET, emptySet()
        )
        for (sku in consumedSubscriptionSkuSet!!) {
            if (managedProduct.inAppPurchase == from(sku)) {
                val family = sharedPreferences.get().getString(getPurchaseFamilyKey(sku), "")
                if (purchaseFamily.name == family) {
                    return true
                }
            }
        }
        return false
    }

    override fun consumePurchase(managedProduct: Subscription, purchaseFamily: PurchaseFamily)
            : Completable {
        return Completable.fromAction {
            val consumedSubscriptionSkuSet: MutableSet<String> =
                HashSet(
                    sharedPreferences.get().getStringSet(KEY_CONSUMED_SUBSCRIPTION_SET, emptySet())
                )
            val sku = managedProduct.inAppPurchase.sku
            if (!consumedSubscriptionSkuSet.contains(sku)) {
                consumedSubscriptionSkuSet.add(sku)
                val editor = sharedPreferences.get().edit()
                editor.putStringSet(KEY_CONSUMED_SUBSCRIPTION_SET, consumedSubscriptionSkuSet)
                editor.putString(getPurchaseFamilyKey(sku), purchaseFamily.name)
                editor.apply()
            }
        }
    }

    private fun getPurchaseFamilyKey(sku: String): String {
        return String.format(Locale.US, FORMAT_KEY_PURCHASE_FAMILY, sku)
    }
}