package com.wops.receiptsgo.ad

import android.app.Activity
import javax.inject.Inject

class EmptyInterstitialAdPresenter @Inject constructor() : InterstitialAdPresenter {

    override fun showAd(activity: Activity) {
        /* no-op */
    }
}