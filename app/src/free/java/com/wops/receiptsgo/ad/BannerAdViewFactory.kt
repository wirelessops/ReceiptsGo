package com.wops.receiptsgo.ad

import com.wops.receiptsgo.ad.upsell.UpsellAdView
import com.wops.core.di.scopes.ActivityScope
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class BannerAdViewFactory @Inject constructor(private val upsellProvider: Provider<UpsellAdView>,
                                              private val adMobProvider: Provider<UpsellAdView>) {

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