package com.wops.receiptsgo.ad

import android.app.Activity
import android.view.View
import com.wops.analytics.Analytics
import com.wops.analytics.events.DataPoint
import com.wops.analytics.events.DefaultDataPointEvent
import com.wops.analytics.events.Events
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.activities.LoginSourceDestination
import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.ad.upsell.UpsellAdView
import com.wops.receiptsgo.config.ConfigurationManager
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.utils.ConfigurableResourceFeature
import com.wops.receiptsgo.utils.UiThread
import com.wops.core.di.scopes.ActivityScope
import com.wops.core.identity.IdentityManager
import javax.inject.Inject

@ActivityScope
class BannerAdPresenter @Inject constructor(
    private val adStatusTracker: AdStatusTracker,
    private val userPreferenceManager: UserPreferenceManager,
    private val bannerAdViewFactory: BannerAdViewFactory,
    private val analytics: Analytics,
    private val identityManager: IdentityManager,
    private val navigationHandler: NavigationHandler<ReceiptsGoActivity>,
    private val configurationManager: ConfigurationManager,
    private val purchaseManager: PurchaseManager,
) : AdPresenter {

    private var adView: BannerAdView? = null
    private var upsellAdView: UpsellAdView? = null
    private var adContainer: View? = null

    override fun onActivityCreated(activity: Activity) {
        this.adView = bannerAdViewFactory.get()
        this.upsellAdView = bannerAdViewFactory.getUpSell()
        this.adContainer = activity.findViewById(R.id.ads_layout)

        Logger.info(this, "Loading ad from {}.", adView!!.javaClass)

        // Always initialize the upsell view
        upsellAdView?.onActivityCreated(activity)

        executeIfWeAreShowingAds {
            adView?.onActivityCreated(activity)
            adView?.setAdLoadListener(object : AdLoadListener {
                override fun onAdLoadSuccess() {
                    UiThread.run {
                        adView?.makeVisible()
                        upsellAdView?.hide()
                    }
                    analytics.record(
                        DefaultDataPointEvent(Events.Ads.AdShown).addDataPoint(
                            DataPoint(
                                "ad",
                                adView?.javaClass!!.simpleName
                            )
                        )
                    )
                }

                override fun onAdLoadFailure() {
                    // If we fail to load, hide it and show the upsell
                    UiThread.run {
                        upsellAdView?.makeVisible()
                        adView?.hide()
                    }
                    Logger.error(this, "Failed to load the desired ad")
                    analytics.record(
                        DefaultDataPointEvent(Events.Purchases.AdUpsellShownOnFailure).addDataPoint(
                            DataPoint("ad", adView?.javaClass!!.simpleName)
                        )
                    )
                }
            })

            try {
                adView?.loadAd(userPreferenceManager[UserPreference.Privacy.EnableAdPersonalization])
            } catch (e: Exception) {
                // Swallowing all exception b/c I'm lazy and don't want to handle activity finishing states or issues with 3p code
                Logger.error(this, "Swallowing ad load exception... ", e)
            }

            upsellAdView?.setOnClickListener {
                analytics.record(Events.Purchases.AdUpsellTapped)

                if (configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel)) {
                    if (identityManager.isLoggedIn) {
                        navigationHandler.navigateToSubscriptionsActivity()
                    } else {
                        navigationHandler.navigateToLoginScreen(LoginSourceDestination.SUBSCRIPTIONS)
                    }
                } else {
                    val proPurchase = when {
                        configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel) -> InAppPurchase.PremiumSubscriptionPlan
                        else -> InAppPurchase.SmartReceiptsPlus
                    }
                    this.purchaseManager.initiatePurchase(proPurchase, PurchaseSource.AdBanner)
                }
            }
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
        adContainer = null
    }

    override fun onSuccessPlusPurchase() {
        Logger.info(this, "Hiding the original ad following a purchase")
        // Clean up our main ad before hiding it
        adView?.onPause()
        adView?.onDestroy()
        adView?.hide()
        upsellAdView?.hide()
        adContainer?.visibility = View.GONE
    }

    private fun executeIfWeAreShowingAds(adFunction: () -> Unit) {
        if (!adStatusTracker.shouldShowAds()) {
            adView?.hide()
            upsellAdView?.hide()
            adContainer?.visibility = View.GONE
        } else {
            adContainer?.visibility = View.VISIBLE
            adFunction()
        }
    }

}
