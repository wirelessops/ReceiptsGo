package co.smartreceipts.android.ad

import android.support.annotation.IntDef
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
        return when (getAdProviderId()) {
            ADMOB_DISPLAY_ID -> adMobProvider.get()
            ADINCUBE_DISPLAY_ID -> adinCubeProvider.get()
            AERSERV_DISPLAY_ID -> aerServProvider.get()
            else -> throw IllegalArgumentException("Unknown ad provider id: $0")
        }
    }

    /**
     * Gets an [UpsellAdView] for this user session
     */
    fun getUpSell(): UpsellAdView {
        return upsellProvider.get()
    }

    /**
     * Returns the desired [AdProviderId] to display
     */
    @AdProviderId
    private fun getAdProviderId(): Int {
        return random.nextInt(AD_PROVIDERS)
    }

    companion object {

        private const val AD_PROVIDERS = 3
        private const val ADMOB_DISPLAY_ID = 0
        private const val ADINCUBE_DISPLAY_ID = 1
        private const val AERSERV_DISPLAY_ID = 2

        @IntDef(ADMOB_DISPLAY_ID, ADINCUBE_DISPLAY_ID, AERSERV_DISPLAY_ID)
        @Retention(AnnotationRetention.SOURCE)
        annotation class AdProviderId

    }
}