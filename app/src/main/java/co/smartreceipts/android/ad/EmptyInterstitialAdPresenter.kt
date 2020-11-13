package co.smartreceipts.android.ad

import javax.inject.Inject

class EmptyInterstitialAdPresenter @Inject constructor() : InterstitialAdPresenter {

    override fun showAd() {
        /* no-op */
    }
}