package co.smartreceipts.android.ad.aerserv

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import co.smartreceipts.android.R
import co.smartreceipts.android.ad.AdLoadListener
import co.smartreceipts.android.ad.BannerAdView
import com.aerserv.sdk.*

import javax.inject.Inject

class AerServAdView @Inject constructor() : BannerAdView {

    private var container: ViewGroup? = null
    private var adView: AerServBanner? = null
    private var activity: Activity? = null
    private var adLoadListener: AdLoadListener? = null

    override fun onActivityCreated(activity: Activity) {
        AerServSdk.init(activity, activity.getString(R.string.aerserv_site_id))

        this.activity = activity
        this.container = activity.findViewById(R.id.ads_layout)
        this.adView = AerServBanner(activity)

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL
        adView!!.layoutParams = params

        container!!.addView(adView)
    }

    override fun loadAd(allowAdPersonalization: Boolean) {
        activity?.let {
            AerServSdk.setGdprConsentFlag(it, allowAdPersonalization)
            val config = AerServConfig(it, it.getString(R.string.aerserv_placement_id))
            config.eventListener = AerServEventListener { event, _ ->
                when(event) {
                    AerServEvent.AD_LOADED -> adLoadListener?.onAdLoadSuccess()
                    AerServEvent.AD_FAILED -> adLoadListener?.onAdLoadFailure()
                }
            }
            adView?.configure(config)!!.show()
        }

    }

    override fun setAdLoadListener(listener: AdLoadListener) {
        this.adLoadListener = listener
    }

    override fun onResume() {
        adView?.play()
    }

    override fun onPause() {
        adView?.pause()
    }

    override fun onDestroy() {
        adView?.kill()
        container?.removeView(adView)
        adView = null
        container = null
        activity = null
    }

    override fun makeVisible() {
        adView?.visibility = View.VISIBLE
    }

    override fun hide() {
        adView?.visibility = View.GONE
    }

    override fun setOnClickListener(listener: View.OnClickListener) {
        // Intentional no-op
    }

}
