package com.wops.receiptsgo;

import android.app.Application;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;
import androidx.multidex.MultiDex;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.crash.CrashReporter;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.ad.MobileAds;
import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.di.AppComponent;
import com.wops.receiptsgo.di.BaseAppModule;
import com.wops.receiptsgo.di.DaggerAppComponent;
import com.wops.receiptsgo.images.PicassoInitializer;
import com.wops.receiptsgo.launch.OnLaunchDataPreFetcher;
import com.wops.receiptsgo.ocr.OcrManager;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;
import com.wops.receiptsgo.purchases.PurchaseManager;
import com.wops.receiptsgo.rating.data.AppRatingPreferencesStorage;
import com.wops.receiptsgo.receipts.editor.currency.CurrencyInitializer;
import com.wops.receiptsgo.receipts.ordering.ReceiptsOrderer;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.subscriptions.SubscriptionsPurchaseTracker;
import com.wops.receiptsgo.sync.cleanup.MarkedForDeletionCleaner;
import com.wops.receiptsgo.rating.InAppReviewManager;
import com.wops.receiptsgo.utils.StrictModeConfiguration;
import com.wops.receiptsgo.utils.WBUncaughtExceptionHandler;
import com.wops.receiptsgo.utils.cache.SmartReceiptsTemporaryFileCache;
import com.wops.receiptsgo.utils.rx.DefaultRxErrorHandler;
import com.wops.receiptsgo.versioning.AppVersionManager;
import com.wops.aws.cognito.CognitoManager;
import com.wops.core.identity.IdentityManager;
import com.wops.push.PushManager;
import com.wops.push.PushManagerProvider;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import wb.android.flex.Flex;

public class ReceiptsGoApplication extends Application implements HasAndroidInjector, PushManagerProvider {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    Flex flex;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    OnLaunchDataPreFetcher onLaunchDataPreFetcher;

    @Inject
    ExtraInitializer extraInitializer;

    @Inject
    CurrencyInitializer currencyInitializer;

    @Inject
    IdentityManager identityManager;

    @Inject
    PurchaseManager purchaseManager;

    @Inject
    PushManager pushManager;

    @Inject
    CognitoManager cognitoManager;

    @Inject
    OcrManager ocrManager;

    @Inject
    CrashReporter crashReporter;

    @Inject
    OrderingPreferencesManager orderingPreferencesManager;

    @Inject
    AppRatingPreferencesStorage appRatingPreferencesStorage;

    @Inject
    Analytics analytics;

    @Inject
    ReceiptsOrderer receiptsOrderer;

    @Inject
    MarkedForDeletionCleaner markedForDeletionCleaner;

    @Inject
    PicassoInitializer picassoInitializer;

    @Inject
    MobileAds mobileAds;

    @Inject
    AppVersionManager appVersionManager;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    SubscriptionsPurchaseTracker subscriptionsPurchaseTracker;

    @Inject
    InAppReviewManager inAppReviewManager;

    private AppComponent appComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

         if (BuildConfig.DEBUG) {
             StrictModeConfiguration.enable();
         }

        appComponent = DaggerAppComponent.builder()
                .baseAppModule(new BaseAppModule(this))
                .build();

        appComponent.inject(this);

        WBUncaughtExceptionHandler.initialize();

        Logger.info(this, "\n\n\n\n Launching App...");

        init();
    }

    @NotNull
    @Override
    public PushManager getPushManager() {
        return appComponent.providePushManager();
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

    /**
     * A simple way to allow us to fetch our database helper for database upgrade tests. I attempted
     * to have dagger automatically inject this into our test modules, but I ended up spending far
     * too much time trying to get a TestAppComponent to work properly. For expediency purposes, I'm
     * just using this getter instead
     *
     * @return the {@link DatabaseHelper}
     */
    @VisibleForTesting
    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    /**
     * Similar to our use of {@link #getDatabaseHelper()} above, we supply this getter for our
     * espresso test modules to simplify how we access this content
     *
     * @return the {@link DateFormatter}
     */
    @VisibleForTesting
    public DateFormatter getDateFormatter() {
        return dateFormatter;
    }

    private void init() {

        // To handle RxJava exceptions
        RxJavaPlugins.setErrorHandler(new DefaultRxErrorHandler(analytics));

        // To configure the Android schedulers as per: https://medium.com/@sweers/rxandroids-new-async-api-4ab5b3ad3e93
        final Scheduler asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> asyncMainThreadScheduler);

        flex.initialize();
        userPreferenceManager.initialize();
        purchaseManager.initialize(this);
        orderingPreferencesManager.initialize();
        dateFormatter.initialize();
        onLaunchDataPreFetcher.loadUserData();
        identityManager.initialize();
        pushManager.initialize();
        cognitoManager.initialize();
        ocrManager.initialize();
        crashReporter.initialize(userPreferenceManager.get(UserPreference.Privacy.EnableCrashTracking));
        receiptsOrderer.initialize();
        picassoInitializer.initialize();
        mobileAds.initialize();
        markedForDeletionCleaner.safelyDeleteAllOutstandingItems();
        extraInitializer.init();
        currencyInitializer.init();

        subscriptionsPurchaseTracker.initialize()
                .subscribe(() -> Logger.info(this, "Successfully initialized"),
                        throwable -> Logger.error(this, "Failed to initialize", throwable));

        PDFBoxResourceLoader.init(getApplicationContext());

        // Clear our cache
        Completable.fromAction(() -> new SmartReceiptsTemporaryFileCache(this).resetCache())
                .subscribeOn(Schedulers.io())
                .subscribe();

        // Check if a new version is available
        appVersionManager.onLaunch();

        // Add launch count for rating prompt monitoring
        appRatingPreferencesStorage.incrementLaunchCount();
    }

}
