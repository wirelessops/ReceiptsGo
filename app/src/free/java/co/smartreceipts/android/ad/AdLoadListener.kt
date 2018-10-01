package co.smartreceipts.android.ad

/**
 * Abstracts the various advertisment loading strategies into a share protocol, ensuring that we can
 * properly trace each
 */
interface AdLoadListener {

    /**
     * Will be called if this Ad loaded successfully
     */
    fun onAdLoadSuccess()

    /**
     * Will be called if this Ad failed to load
     */
    fun onAdLoadFailure()
}