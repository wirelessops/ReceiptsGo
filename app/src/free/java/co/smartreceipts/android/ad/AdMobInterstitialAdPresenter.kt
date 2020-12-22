package co.smartreceipts.android.ad

import android.content.Context
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import javax.inject.Inject

@ApplicationScope
class AdMobInterstitialAdPresenter @Inject constructor(context: Context, private val purchaseWallet: PurchaseWallet) :
    InterstitialAdPresenter {

    private var interstitialAd: InterstitialAd = InterstitialAd(context)

    init {
        interstitialAd.adUnitId = context.getString(R.string.admob_interstitial_ad_unit_id)

        interstitialAd.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: Int) {
                Logger.error(this, "Interstitial ad failed to load, error code = $p0")
            }

            override fun onAdClosed() {
                preload()
            }
        }

        preload()
    }

    override fun showAd() {
        if (purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)) {
            return
        }

        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        } else when {
            // if the ad was failed to load, trying to load again for future
            !interstitialAd.isLoading && !interstitialAd.isLoaded -> preload()
        }
    }

    private fun preload() {
        interstitialAd.loadAd(AdRequest.Builder().build())
    }
}