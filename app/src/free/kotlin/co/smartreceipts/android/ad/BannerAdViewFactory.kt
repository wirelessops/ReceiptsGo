package co.smartreceipts.android.ad

import co.smartreceipts.android.ad.admob.AdMobAdView
import co.smartreceipts.android.ad.upsell.UpsellAdView
import co.smartreceipts.android.di.scopes.ActivityScope
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class BannerAdViewFactory @Inject constructor(private val upsellProvider: Provider<UpsellAdView>,
                                              private val adMobProvider: Provider<AdMobAdView>) {

    /**
     * Fetches a the appropriate [BannerAdView] for this user session
     */
    fun get(): BannerAdView {
        return adMobProvider.get()
    }

    /**
     * Gets an [UpsellAdView] for this user session
     */
    fun getUpSell(): UpsellAdView {
        return upsellProvider.get()
    }

}