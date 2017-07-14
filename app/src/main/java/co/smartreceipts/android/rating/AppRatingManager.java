package co.smartreceipts.android.rating;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.rating.data.AppRatingStorage;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class AppRatingManager {

    // AppRating (Use a combination of launches and a timer for the app rating
    // to ensure that we aren't prompting new users too soon
    private static final int LAUNCHES_UNTIL_PROMPT = 15;
    private static final int DAYS_UNTIL_PROMPT = 7;

    private AppRatingStorage appRatingStorage;

    @Inject
    AppRatingManager(AppRatingStorage appRatingStorage) {
        this.appRatingStorage = appRatingStorage;

        setCustomUncaughtExceptionHandler();
    }

    public Single<Boolean> checkIfNeedToAskRating() {
        return appRatingStorage.readAppRatingData()
                .map(appRatingModel -> {
                    if (appRatingModel.canShow() && !appRatingModel.isCrashOccurred()) {
                        // Check if we've reached a rating event
                        final long daysToMillis = TimeUnit.DAYS.toMillis(1);
                        if (appRatingModel.getLaunchCount() >= LAUNCHES_UNTIL_PROMPT + appRatingModel.getAdditionalLaunchThreshold() &&
                                (System.currentTimeMillis() - appRatingModel.getInstallTime()) / daysToMillis >= DAYS_UNTIL_PROMPT) {
                            return true;
                        }
                    }
                    return false;
                })
                .subscribeOn(Schedulers.io());
    }

    public void dontShowRatingPromptAgain() {
        appRatingStorage.setDontShowRatingPromptMore();
    }

    public void prorogueRatingPrompt() {
        appRatingStorage.prorogueRatingPrompt(LAUNCHES_UNTIL_PROMPT);
    }

    private void setCustomUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!(exceptionHandler instanceof RatingUncaughtExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new RatingUncaughtExceptionHandler(appRatingStorage, exceptionHandler));
        }
    }

    private static final class RatingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final AppRatingStorage sAppRatingStorage;
        private final Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;

        public RatingUncaughtExceptionHandler(AppRatingStorage appRatingStorage, Thread.UncaughtExceptionHandler exceptionHandler) {
            sAppRatingStorage = appRatingStorage;
            sUncaughtExceptionHandler = exceptionHandler;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            sAppRatingStorage.crashOccurred();
            sUncaughtExceptionHandler.uncaughtException(thread, throwable);
        }

    }
}
