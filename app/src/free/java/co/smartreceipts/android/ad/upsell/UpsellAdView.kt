package co.smartreceipts.android.ad.upsell

import android.app.Activity
import android.view.View
import co.smartreceipts.android.R
import co.smartreceipts.android.ad.AdLoadListener
import co.smartreceipts.android.ad.BannerAdView
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
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
        upsellButton?.visibility = View.VISIBLE
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
