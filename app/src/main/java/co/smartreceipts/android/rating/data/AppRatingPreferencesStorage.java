package co.smartreceipts.android.rating.data;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;
import javax.inject.Named;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class AppRatingPreferencesStorage implements AppRatingStorage {

    /**
     * Key to get rating preferences
     */
    public static final String RATING_PREFERENCES = "Smart Receipts rating";

    private static final class Keys {
        /**
         * Key to track user preference about no longer showing rating window
         */
        private static final String DONT_SHOW = "dont_show";

        /**
         * Key to track how many times the user has launched the application
         */
        private static final String LAUNCH_COUNT = "launches";

        /**
         * Key to track if the users wishes to be reminded later
         */
        private static final String ADDITIONAL_LAUNCH_THRESHOLD = "threshold";

        /**
         * Key to track the first call of {@link AppRatingStorage#incrementLaunchCount()} method in millis
         */
        private static final String INSTALL_TIME_MILLIS = "days";

        /**
         * Key to track if the application crashed at a prior date
         */
        private static final String CRASH_OCCURRED = "hide_on_crash";
    }

    private final Lazy<SharedPreferences> sharedPreferences;

    @Inject
    public AppRatingPreferencesStorage(@NonNull @Named(RATING_PREFERENCES) Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    @Override
    public Single<AppRatingModel> readAppRatingData() {
        return Single.fromCallable(() -> {
            final SharedPreferences preferences = sharedPreferences.get();
            // Set up some vars
            long now = System.currentTimeMillis();
            // Get our current values
            boolean canShow = !preferences.getBoolean(Keys.DONT_SHOW, false);
            boolean crashOccurred = preferences.getBoolean(Keys.CRASH_OCCURRED, false);
            int launchCount = preferences.getInt(Keys.LAUNCH_COUNT, 0);
            int additionalLaunchThreshold = preferences.getInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, 0);
            long installTime = preferences.getLong(Keys.INSTALL_TIME_MILLIS, now);

            return new AppRatingModel(canShow, crashOccurred, launchCount, additionalLaunchThreshold,
                    installTime);
        });
    }

    @Override
    public void incrementLaunchCount() {
        Completable.fromAction(() -> {
                    SharedPreferences.Editor editor = sharedPreferences.get().edit();
                    int currentLaunchCount = sharedPreferences.get().getInt(Keys.LAUNCH_COUNT, 0);
                    if (currentLaunchCount == 0) {
                        editor.putLong(Keys.INSTALL_TIME_MILLIS, System.currentTimeMillis());
                    }
                    editor.putInt(Keys.LAUNCH_COUNT, currentLaunchCount + 1).apply();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @Override
    public void setDontShowRatingPromptMore() {
        sharedPreferences.get().edit()
                .putBoolean(Keys.DONT_SHOW, true)
                .apply();
    }

    @Override
    public void crashOccurred() {
        sharedPreferences.get().edit()
                .putBoolean(Keys.CRASH_OCCURRED, true)
                .apply();
    }

    @Override
    public void prorogueRatingPrompt(int prorogueLaunches) {
        int oldAdditionalLaunches = sharedPreferences.get().getInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, 0);
        sharedPreferences.get().edit()
                .putInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, oldAdditionalLaunches + prorogueLaunches)
                .putBoolean(Keys.DONT_SHOW, false)
                .apply();
    }

}
