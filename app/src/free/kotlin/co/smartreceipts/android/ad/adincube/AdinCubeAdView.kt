package co.smartreceipts.android.ad.adincube

import android.app.Activity
import android.view.View
import android.view.ViewGroup

import co.smartreceipts.android.R
import co.smartreceipts.android.ad.AdLoadListener
import co.smartreceipts.android.ad.BannerAdView
import com.adincube.sdk.AdinCube
import com.adincube.sdk.AdinCubeBannerEventListener
import com.adincube.sdk.BannerView

import javax.inject.Inject

class AdinCubeAdView @Inject constructor() : BannerAdView {

    private var container: ViewGroup? = null
    private var adView: BannerView? = null
    private var activity: Activity? = null
    private var adLoadListener: AdLoadListener? = null

    override fun onActivityCreated(activity: Activity) {
        AdinCube.setAppKey(activity.getString(R.string.aerServAdKey))

        this.activity = activity
        this.container = activity.findViewById(R.id.adView_container)
        this.adView = AdinCube.Banner.createView(activity, AdinCube.Banner.Size.BANNER_AUTO)

        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        adView!!.layoutParams = params
        adView!!.setEventListener(object : AdinCubeBannerEventListener {
            override fun onAdClicked(view: BannerView?) {
                // Intentional no-op
            }

            override fun onAdShown(view: BannerView?) {
                // Intentional no-op
            }

            override fun onLoadError(view: BannerView?, errorCode: String?) {
                adLoadListener?.onAdLoadFailure()
            }

            override fun onError(view: BannerView?, errorCode: String?) {
                adLoadListener?.onAdLoadFailure()
            }

            override fun onAdLoaded(view: BannerView?) {
                adLoadListener?.onAdLoadSuccess()
            }

        })
        container!!.addView(adView)
    }

    override fun loadAd(allowAdPersonalization: Boolean) {
        if (allowAdPersonalization) {
            AdinCube.UserConsent.setAccepted(activity)
        } else {
            AdinCube.UserConsent.setDeclined(activity)
        }
        adView?.load()
    }

    override fun setAdLoadListener(listener: AdLoadListener) {
        this.adLoadListener = listener
    }

    override fun onResume() {
        // Intentional no-op
    }

    override fun onPause() {
        // Intentional no-op
    }

    override fun onDestroy() {
        adView?.destroy()
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
