package com.wops.receiptsgo.ad.admob

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.ad.AdLoadListener
import com.wops.receiptsgo.ad.BannerAdView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import javax.inject.Inject

class AdMobAdView @Inject constructor() : BannerAdView {

    private var container: ViewGroup? = null
    private var adView: AdView? = null
    private var adLoadListener: AdLoadListener? = null

    override fun onActivityCreated(activity: Activity) {
        this.container = activity.findViewById(R.id.ads_layout)
        this.adView = AdView(activity)

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        adView!!.layoutParams = params
        adView!!.setAdSize(AdSize.SMART_BANNER)
        adView!!.adUnitId = activity.resources.getString(R.string.classicAdUnitId)
        adView!!.adListener = object: AdListener() {
            override fun onAdLoaded() {
                adLoadListener?.onAdLoadSuccess()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                adLoadListener?.onAdLoadFailure()
            }
        }
        container!!.addView(adView)
    }

    override fun loadAd(allowAdPersonalization: Boolean) {
        // This method is really slow and cannot be moved off the main thread (ugh...)
        // We post it with a delay, so it only happens after the core UI of the app starts to load
        adView?.postDelayed({
            try {
                adView?.loadAd(getAdRequest(allowAdPersonalization))
            } catch (e: Exception) {
                // Swallowing all exception b/c I'm lazy and don't want to handle activity finishing states
                Logger.error(this, "Swallowing ad load exception... ", e)
            }
        }, LOADING_DELAY.toLong())
    }

    override fun setAdLoadListener(listener: AdLoadListener) {
        this.adLoadListener = listener
    }

    override fun onResume() {
        adView?.resume()
    }

    override fun onPause() {
        adView?.pause()
    }

    override fun onDestroy() {
        adView?.destroy()
        container?.removeView(adView)
        adView = null
        container = null
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

    companion object {

        private val LOADING_DELAY = 50 // millis

        private fun getAdRequest(allowAdPersonalization: Boolean): AdRequest {
            val builder = AdRequest.Builder()
            if (allowAdPersonalization) {
                val extras = Bundle()
                extras.putString("npa", "1")
                builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            }
            return builder.build()
        }
    }
}
