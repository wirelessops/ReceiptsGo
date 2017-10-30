package co.smartreceipts.android.rating.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import io.reactivex.Single;

@ApplicationScope
public class AppRatingPreferencesStorage implements AppRatingStorage {

    @Inject
    Context appContext;

    private static final class Keys {
        /**
         * Key to get rating preferences
         */
        private static final String RATING_PREFERENCES = "Smart Receipts rating";
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

    @Inject
    public AppRatingPreferencesStorage() {
    }

    @Override
    public Single<AppRatingModel> readAppRatingData() {
        return Single.fromCallable(new Callable<AppRatingModel>() {
            @Override
            public AppRatingModel call() throws Exception {
                SharedPreferences sharedPreferences = getSharedPreferences();

                // Set up some vars
                long now = System.currentTimeMillis();
                // Get our current values
                boolean canShow = !sharedPreferences.getBoolean(Keys.DONT_SHOW, false);
                boolean crashOccurred = sharedPreferences.getBoolean(Keys.CRASH_OCCURRED, false);
                int launchCount = sharedPreferences.getInt(Keys.LAUNCH_COUNT, 0);
                int additionalLaunchThreshold = sharedPreferences.getInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, 0);
                long installTime = sharedPreferences.getLong(Keys.INSTALL_TIME_MILLIS, now);

                return new AppRatingModel(canShow, crashOccurred, launchCount, additionalLaunchThreshold,
                        installTime);
            }
        });
    }

    @Override
    public void incrementLaunchCount() {
        SharedPreferences.Editor editor = getPreferencesEditor();
        int currentLaunchCount = getSharedPreferences().getInt(Keys.LAUNCH_COUNT, 0);
        if (currentLaunchCount == 0) {
            editor.putLong(Keys.INSTALL_TIME_MILLIS, System.currentTimeMillis());
        }
        editor.putInt(Keys.LAUNCH_COUNT, currentLaunchCount + 1).apply();
    }

    @Override
    public void setDontShowRatingPromptMore() {
        getPreferencesEditor()
                .putBoolean(Keys.DONT_SHOW, true)
                .apply();
    }

    @Override
    public void crashOccurred() {
        getPreferencesEditor()
                .putBoolean(Keys.CRASH_OCCURRED, true)
                .apply();
    }

    @Override
    public void prorogueRatingPrompt(int prorogueLaunches) {
        int oldAdditionalLaunches = getSharedPreferences().getInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, 0);
        getPreferencesEditor()
                .putInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, oldAdditionalLaunches + prorogueLaunches)
                .putBoolean(Keys.DONT_SHOW, false)
                .apply();
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        return getSharedPreferences().edit();
    }

    private SharedPreferences getSharedPreferences() {
        return appContext.getSharedPreferences(Keys.RATING_PREFERENCES, Context.MODE_PRIVATE);
    }

}
