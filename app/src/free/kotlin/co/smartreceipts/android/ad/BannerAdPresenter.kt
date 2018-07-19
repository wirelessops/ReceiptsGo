package co.smartreceipts.android.ad

import android.app.Activity
import android.content.Context
import android.view.View
import co.smartreceipts.android.ad.upsell.UpsellAdView

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.DataPoint
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.ActivityScope
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions
import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.source.PurchaseSource
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.UiThread
import co.smartreceipts.android.utils.log.Logger
import javax.inject.Inject

@ActivityScope
class BannerAdPresenter @Inject constructor(private val context: Context,
                                            private val purchaseWallet: PurchaseWallet,
                                            private val purchaseManager: PurchaseManager,
                                            private val userPreferenceManager: UserPreferenceManager,
                                            private val bannerAdViewFactory: BannerAdViewFactory,
                                            private val analytics: Analytics) : AdPresenter {



    private var adView: BannerAdView? = null
    private var upsellAdView: UpsellAdView? = null

    override fun onActivityCreated(activity: Activity) {
        this.adView = bannerAdViewFactory.get()
        this.upsellAdView = bannerAdViewFactory.getUpSell()

        Logger.info(this, "Loading ad from {}.", adView!!.javaClass)

        // Always initialize the upsell view
        upsellAdView?.onActivityCreated(activity)

        executeIfWeAreShowingAds {
            adView?.onActivityCreated(activity)
            adView?.setAdLoadListener(object : AdLoadListener {
                override fun onAdLoadSuccess() {
                    UiThread.run {
                        adView?.makeVisible()
                    }
                    analytics.record(DefaultDataPointEvent(Events.Ads.AdShown).addDataPoint(DataPoint("ad", adView?.javaClass!!.simpleName)))
                }

                override fun onAdLoadFailure() {
                    // If we fail to load, hide it and show the upsell
                    UiThread.run {
                        upsellAdView?.makeVisible()
                    }
                    Logger.error(this, "Failed to load the desired ad")
                    analytics.record(DefaultDataPointEvent(Events.Purchases.AdUpsellShownOnFailure).addDataPoint(DataPoint("ad", adView?.javaClass!!.simpleName)))
                }
            })

            try {
                adView?.loadAd(userPreferenceManager.get(UserPreference.Privacy.EnableAdPersonalization))
            } catch (e: Exception) {
                // Swallowing all exception b/c I'm lazy and don't want to handle activity finishing states or issues with 3p code
                Logger.error(this, "Swallowing ad load exception... ", e)
            }

            upsellAdView?.setOnClickListener(View.OnClickListener {
                analytics.record(Events.Purchases.AdUpsellTapped)
                this.purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.AdBanner)
            })
        }
    }

    override fun onResume() {
        upsellAdView?.onResume()
        executeIfWeAreShowingAds { adView?.onResume() }
    }

    override fun onPause() {
        upsellAdView?.onPause()
        executeIfWeAreShowingAds { adView?.onPause() }
    }

    override fun onDestroy() {
        upsellAdView?.onDestroy()
        executeIfWeAreShowingAds { adView?.onDestroy() }
    }

    override fun onSuccessPlusPurchase() {
        Logger.info(this, "Hiding the original ad following a purchase")
        // Clean up our main ad before hiding it
        adView?.onPause()
        adView?.onDestroy()
        adView?.hide()
        upsellAdView?.hide()
    }

    private fun executeIfWeAreShowingAds(adFunction: () -> Unit) {
        if (!shouldShowAds(context)) {
            adView?.hide()
            upsellAdView?.hide()
        } else {
            adFunction()
        }
    }

    private fun shouldShowAds(activityContext: Context): Boolean {
        val hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
        val areAdsEnabledLocally = activityContext.getSharedPreferences(AD_PREFERENECES, 0).getBoolean(SHOW_AD, true)
        return areAdsEnabledLocally && !hasProSubscription
    }

    companion object {

        //Preference Identifiers - SubClasses Only
        private val AD_PREFERENECES = SharedPreferenceDefinitions.SubclassAds.toString()
        private val SHOW_AD = "pref1"
    }
}
