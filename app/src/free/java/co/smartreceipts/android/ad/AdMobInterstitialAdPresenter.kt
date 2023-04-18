package co.smartreceipts.android.ad

import android.app.Activity
import android.content.Context
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject

@ApplicationScope
class AdMobInterstitialAdPresenter @Inject constructor(
    private val context: Context,
    private val adStatusTracker: AdStatusTracker
) : InterstitialAdPresenter {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    init {
        preload()
    }

    override fun showAd(activity: Activity) {
        if (!adStatusTracker.shouldShowAds()) {
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    preload()
                }
            }

            interstitialAd?.show(activity)

        } else when {
            // if the ad was failed to load, trying to load again for future
            !isLoading -> preload()
        }
    }

    private fun preload() {
        val adUnitId = context.getString(R.string.admob_interstitial_ad_unit_id)
        val adRequest = AdRequest.Builder().build()

        isLoading = true
        interstitialAd = null

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.error(this, "Interstitial ad failed to load, error code = ${adError.code}")

                isLoading = false
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                isLoading = false
                interstitialAd = ad
            }
        })
    }
}