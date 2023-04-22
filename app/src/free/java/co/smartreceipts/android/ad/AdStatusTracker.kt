package co.smartreceipts.android.ad

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.core.di.scopes.ApplicationScope
import javax.inject.Inject


/**
 * A wrapper class that allows us to quickly determine if we should show this user ads or not
 */
@ApplicationScope
class AdStatusTracker @Inject constructor(private val context: Context,
                                          private val purchaseWallet: PurchaseWallet) {

    /**
     * Checks if we should show ads. Technically this should only be called from an [WorkerThread],
     * but I have relaxed this requirement to suit some of the limitations that we see in our 3p
     * ad provider libraries (which require lots of UI Thread interactions).
     *
     * Internally, everything relies on [SharedPreferences], which caches everything after the first
     * load. As a result, it's *mostly* safe to call this off the UI thread. To avoid risking anything,
     * however, it is recommended that we pre-fetch this data from a background thread in App.onCreate.
     */
    fun shouldShowAds() : Boolean {
        val hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                || purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)
        val areAdsEnabledLocally = context.getSharedPreferences(AD_PREFERENCES, 0).getBoolean(SHOW_AD, true)
        return areAdsEnabledLocally && !hasProSubscription
    }

    companion object {

        private val AD_PREFERENCES = SharedPreferenceDefinitions.SubclassAds.toString()
        private const val SHOW_AD = "pref1"

    }
}