package co.smartreceipts.android.ad

import co.smartreceipts.android.ad.admob.AdMobAdView
import co.smartreceipts.android.ad.upsell.UpsellAdView
import co.smartreceipts.android.di.scopes.ActivityScope
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class BannerAdViewFactory @Inject constructor(private val upsellProvider: Provider<UpsellAdView>,
                                              private val adMobProvider: Provider<AdMobAdView>) {

    private val random = Random()

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

    /**
     * Checks if should show a given ad type, based out of a random frequency check out of 100
     */
    private fun showShowAd(adTypeDisplayFrequency: Int): Boolean {
        return adTypeDisplayFrequency >= random.nextInt(RANDOM_MAX + 1)
    }

    companion object {

        private const val RANDOM_MAX = 100

    }
}