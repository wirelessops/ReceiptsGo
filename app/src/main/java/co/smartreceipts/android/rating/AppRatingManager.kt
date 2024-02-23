package co.smartreceipts.android.rating

import co.smartreceipts.android.rating.data.AppRatingModel
import co.smartreceipts.android.rating.data.AppRatingStorage
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ApplicationScope
class AppRatingManager @Inject internal constructor(private val appRatingStorage: AppRatingStorage) {

    init {
        setCustomUncaughtExceptionHandler()
    }

    fun checkIfNeedToAskRating(): Single<Boolean> {
        return appRatingStorage.readAppRatingData()
            .map { appRatingModel: AppRatingModel ->
                if (appRatingModel.canShow && !appRatingModel.isCrashOccurred && !appRatingModel.inAppReviewShown) {
                    // Check if we've reached a rating event
                    val daysToMillis = TimeUnit.DAYS.toMillis(1)
                    if (appRatingModel.launchCount >= LAUNCHES_UNTIL_PROMPT + appRatingModel.additionalLaunchThreshold &&
                        (System.currentTimeMillis() - appRatingModel.installTime) / daysToMillis >= DAYS_UNTIL_PROMPT
                    ) {
                        return@map true
                    }
                }
                false
            }
            .subscribeOn(Schedulers.io())
    }

    fun dontShowRatingPromptAgain() {
        appRatingStorage.setDontShowRatingPromptMore()
    }

    fun prorogueRatingPrompt() {
        appRatingStorage.prorogueRatingPrompt(LAUNCHES_UNTIL_PROMPT)
    }

    private fun setCustomUncaughtExceptionHandler() {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (exceptionHandler != null && exceptionHandler !is RatingUncaughtExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(
                RatingUncaughtExceptionHandler(
                    appRatingStorage,
                    exceptionHandler
                )
            )
        }
    }

    private class RatingUncaughtExceptionHandler(
        private val sAppRatingStorage: AppRatingStorage,
        private val sUncaughtExceptionHandler: Thread.UncaughtExceptionHandler
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            sAppRatingStorage.crashOccurred()
            sUncaughtExceptionHandler.uncaughtException(thread, throwable)
        }
    }

    companion object {

        // AppRating (Use a combination of launches and a timer for the app rating
        // to ensure that we aren't prompting new users too soon
        private const val LAUNCHES_UNTIL_PROMPT = 15
        private const val DAYS_UNTIL_PROMPT = 7
    }
}
