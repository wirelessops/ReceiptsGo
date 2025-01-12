package com.wops.receiptsgo.ad

import android.app.Activity
import android.view.View.OnClickListener

/**
 * Defines a generic contract by which we can interact with various Ad SDKs
 */
interface BannerAdView {

    /**
     * Called whenever the parent lifecycle onActivityCreated method is called
     */
    fun onActivityCreated(activity: Activity)

    /**
     * Indicates that we should loading this advertisement. Results of this may be consumed via a
     * [AdLoadListener], which can be set via [BannerAdView.setAdLoadListener]
     *
     * @param allowAdPersonalization, which indicates if we're allowing personalized ads or not
     */
    fun loadAd(allowAdPersonalization: Boolean)

    /**
     * Called to indicate that this Ad should be made visible
     */
    fun makeVisible()

    /**
     * Called to indicate that this Ad should be made invisible
     */
    fun hide()

    /**
     * Called whenever the parent lifecycle onResume method is called
     */
    fun onResume()

    /**
     * Called whenever the parent lifecycle onPause method is called
     */
    fun onPause()

    /**
     * Called whenever the parent lifecycle onDestroy method is called
     */
    fun onDestroy()

    /**
     * Sets an [AdLoadListener], which will be informed about the results of [BannerAdView.loadAd]
     */
    fun setAdLoadListener(listener: AdLoadListener)

    /**
     * Sets a [OnClickListener], which will be called when this Ad is clicked
     */
    fun setOnClickListener(listener: OnClickListener)

}
