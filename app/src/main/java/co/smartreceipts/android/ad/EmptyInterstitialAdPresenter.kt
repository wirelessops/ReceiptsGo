package co.smartreceipts.android.ad

import android.app.Activity
import javax.inject.Inject

class EmptyInterstitialAdPresenter @Inject constructor() : InterstitialAdPresenter {

    override fun showAd(activity: Activity) {
        /* no-op */
    }
}