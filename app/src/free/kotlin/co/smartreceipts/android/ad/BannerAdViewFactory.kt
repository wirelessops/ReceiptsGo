package co.smartreceipts.android.ad

import co.smartreceipts.android.ad.adincube.AdinCubeAdView
import co.smartreceipts.android.ad.admob.AdMobAdView
import co.smartreceipts.android.ad.aerserv.AerServAdView
import co.smartreceipts.android.ad.upsell.UpsellAdView
import co.smartreceipts.android.di.scopes.ActivityScope
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class BannerAdViewFactory @Inject constructor(private val upsellProvider: Provider<UpsellAdView>,
                                              private val adMobProvider: Provider<AdMobAdView>,
                                              private val adinCubeProvider: Provider<AdinCubeAdView>,
                                              private val aerServProvider: Provider<AerServAdView>) {

    private val random = Random()

    /**
     * Fetches a the appropriate [BannerAdView] for this user session
     */
    fun get(): BannerAdView {
        if (false) {
            if (shouldShowAd(ADINCUBE_DISPLAY_FREQUENCY)) {
                return adinCubeProvider.get()
            } else if (shouldShowAd(AERSERV_DISPLAY_FREQUENCY)) {
                return aerServProvider.get()
            } else {
                return adMobProvider.get()
            }
        } else {
            return aerServProvider.get()
        }
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
    private fun shouldShowAd(adTypeDisplayFrequency: Int): Boolean {
        return adTypeDisplayFrequency >= random.nextInt(RANDOM_MAX + 1)
    }

    companion object {

        private const val RANDOM_MAX = 100
        private const val ADINCUBE_DISPLAY_FREQUENCY = 33
        private const val AERSERV_DISPLAY_FREQUENCY = 33

    }
}