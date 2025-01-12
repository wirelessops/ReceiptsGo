package com.wops.receiptsgo.ad.upsell

import android.app.Activity
import android.view.View
import com.wops.analytics.Analytics
import com.wops.analytics.events.Events
import com.wops.receiptsgo.R
import com.wops.receiptsgo.ad.AdLoadListener
import com.wops.receiptsgo.ad.BannerAdView
import javax.inject.Inject

class UpsellAdView @Inject constructor(private val analytics: Analytics) : BannerAdView {

    private var upsellButton: View? = null
    private var adLoadListener: AdLoadListener? = null

    override fun onActivityCreated(activity: Activity) {
        this.upsellButton = activity.findViewById(R.id.adView_upsell)
    }

    override fun loadAd(allowAdPersonalization: Boolean) {
        // Upsell loads are always successful :)
        adLoadListener?.onAdLoadSuccess()
    }

    override fun makeVisible() {
        analytics.record(Events.Purchases.AdUpsellShown)
        upsellButton?.visibility = View.GONE
    }

    override fun hide() {
        upsellButton?.visibility = View.GONE
    }

    override fun onResume() {
        // Intentional no-op
    }

    override fun onPause() {
        // Intentional no-op
    }

    override fun onDestroy() {
        this.upsellButton = null
    }

    override fun setAdLoadListener(listener: AdLoadListener) {
        this.adLoadListener = listener
    }

    override fun setOnClickListener(listener: View.OnClickListener) {
        upsellButton?.setOnClickListener(listener)
    }

}
