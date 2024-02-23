package co.smartreceipts.android.rating

import android.app.Activity
import android.content.Context
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.rating.data.AppRatingStorage
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.Single
import javax.inject.Inject

@ApplicationScope
class InAppReviewManager @Inject constructor(context: Context, private val appRatingStorage: AppRatingStorage) {

    private val manager = ReviewManagerFactory.create(context)
    private var reviewInfo: ReviewInfo? = null

    init {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            } else {
                Logger.warn(this, "[in-app review] Failed to get in-app review info", task.exception)
            }
        }
    }

    fun canShowReview(): Single<Boolean> {
        return appRatingStorage.readAppRatingData().map { appRatingModel ->
            appRatingModel.canShow
                    && appRatingModel.inAppReviewShown.not()
                    && reviewInfo != null
        }
    }

    fun showReview(activity: Activity) {
        reviewInfo?.let { reviewInfo ->
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
                Logger.warn(this, "[in-app review] In-app review complete")

                appRatingStorage.setInAppReviewShown()
            }
        }
    }
}