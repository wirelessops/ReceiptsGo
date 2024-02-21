package co.smartreceipts.android.utils

import android.app.Activity
import android.content.Context
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import javax.inject.Inject

@ApplicationScope
class InAppReviewManager @Inject constructor(context: Context) {

    private val manager = ReviewManagerFactory.create(context)
    private var reviewInfo: ReviewInfo? = null

    init {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            } else {
                Logger.warn(this, "Failed to get in-app review info", task.exception)
            }
        }
    }

    val isReviewAvailable get() = reviewInfo != null

    fun showReview(activity: Activity) {
        reviewInfo?.let { reviewInfo ->
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
                Logger.warn(this, "In-app review complete")
            }
        }
    }
}