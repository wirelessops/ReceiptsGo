package com.wops.receiptsgo.rating.data

import android.content.SharedPreferences
import com.wops.core.di.scopes.ApplicationScope
import com.google.common.base.Preconditions
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class AppRatingPreferencesStorage @Inject constructor(@Named(RATING_PREFERENCES) sharedPreferences: Lazy<SharedPreferences>) :
    AppRatingStorage {

    private val sharedPreferences: Lazy<SharedPreferences>

    init {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences)
    }

    override fun readAppRatingData(): Single<AppRatingModel> {
        return Single.fromCallable {
            val preferences = sharedPreferences.get()
            // Set up some vars
            val now = System.currentTimeMillis()
            // Get our current values
            val canShow = !preferences.getBoolean(DONT_SHOW, false)
            val crashOccurred = preferences.getBoolean(CRASH_OCCURRED, false)
            val launchCount = preferences.getInt(LAUNCH_COUNT, 0)
            val additionalLaunchThreshold = preferences.getInt(ADDITIONAL_LAUNCH_THRESHOLD, 0)
            val installTime = preferences.getLong(INSTALL_TIME_MILLIS, now)
            val inAppReviewShown = preferences.getBoolean(IN_APP_REVIEW_SHOWN, false)

            AppRatingModel(
                canShow = canShow,
                isCrashOccurred = crashOccurred,
                launchCount = launchCount,
                additionalLaunchThreshold = additionalLaunchThreshold,
                installTime = installTime,
                inAppReviewShown = inAppReviewShown
            )
        }
    }

    override fun incrementLaunchCount() {
        Completable.fromAction {
            val editor = sharedPreferences.get().edit()
            val currentLaunchCount = sharedPreferences.get().getInt(LAUNCH_COUNT, 0)
            if (currentLaunchCount == 0) {
                editor.putLong(INSTALL_TIME_MILLIS, System.currentTimeMillis())
            }
            editor.putInt(LAUNCH_COUNT, currentLaunchCount + 1).apply()
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun setDontShowRatingPromptMore() {
        sharedPreferences.get().edit()
            .putBoolean(DONT_SHOW, true)
            .apply()
    }

    override fun crashOccurred() {
        sharedPreferences.get().edit()
            .putBoolean(CRASH_OCCURRED, true)
            .apply()
    }

    override fun setInAppReviewShown() {
        sharedPreferences.get().edit()
            .putBoolean(IN_APP_REVIEW_SHOWN, true)
            .apply()
    }

    override fun prorogueRatingPrompt(prorogueLaunches: Int) {
        val oldAdditionalLaunches = sharedPreferences.get().getInt(ADDITIONAL_LAUNCH_THRESHOLD, 0)
        sharedPreferences.get().edit()
            .putInt(ADDITIONAL_LAUNCH_THRESHOLD, oldAdditionalLaunches + prorogueLaunches)
            .putBoolean(DONT_SHOW, false)
            .apply()
    }

    companion object {

        /**
         * Key to get rating preferences
         */
        const val RATING_PREFERENCES = "Smart Receipts rating"

        /**
         * Key to track user preference about no longer showing rating window
         */
        private const val DONT_SHOW = "dont_show"

        /**
         * Key to track how many times the user has launched the application
         */
        private const val LAUNCH_COUNT = "launches"

        /**
         * Key to track if the users wishes to be reminded later
         */
        private const val ADDITIONAL_LAUNCH_THRESHOLD = "threshold"

        /**
         * Key to track the first call of [AppRatingStorage.incrementLaunchCount] method in millis
         */
        private const val INSTALL_TIME_MILLIS = "days"

        /**
         * Key to track if the application crashed at a prior date
         */
        private const val CRASH_OCCURRED = "hide_on_crash"

        /**
         * Key to track if the in-app review dialog has already been shown
         */
        private const val IN_APP_REVIEW_SHOWN = "in_app_review_shown"
    }
}
