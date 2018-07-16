package co.smartreceipts.android;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.LeakCanary;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.crash.CrashReporter;
import co.smartreceipts.android.aws.cognito.CognitoManager;
import co.smartreceipts.android.di.AppComponent;
import co.smartreceipts.android.di.BaseAppModule;
import co.smartreceipts.android.di.DaggerAppComponent;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.images.PicassoInitializer;
import co.smartreceipts.android.launch.OnLaunchDataPreFetcher;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.push.PushManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.versions.AppVersionManager;
import co.smartreceipts.android.settings.versions.VersionUpgradedListener;
import co.smartreceipts.android.sync.cleanup.MarkedForDeletionCleaner;
import co.smartreceipts.android.utils.WBUncaughtExceptionHandler;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.utils.rx.DefaultRxErrorHandler;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.Completable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import wb.android.flex.Flex;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

public class SmartReceiptsApplication extends Application implements VersionUpgradedListener,
        HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> serviceInjector;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    Flex flex;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    OnLaunchDataPreFetcher onLaunchDataPreFetcher;

    @Inject
    ExtraInitializer extraInitializer;

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
            Logger.debug(this, "Enabling strict mode");
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());


            final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
            vmPolicyBuilder.detectActivityLeaks();
            vmPolicyBuilder.detectFileUriExposure();
            vmPolicyBuilder.detectLeakedClosableObjects();
            vmPolicyBuilder.detectLeakedRegistrationObjects();
            vmPolicyBuilder.detectLeakedSqlLiteObjects();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                vmPolicyBuilder.detectCleartextNetwork();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vmPolicyBuilder.detectContentUriWithoutPermission();
            }
            // vmPolicyBuilder.detectUntaggedSockets(); Note: We exclude this one as many of our 3p libraries fail it
            vmPolicyBuilder.penaltyLog();
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }

        appComponent = DaggerAppComponent.builder()
                .baseAppModule(new BaseAppModule(this))
                .build();

        appComponent.inject(this);

        WBUncaughtExceptionHandler.initialize();

        Logger.info(this, "\n\n\n\n Launching App...");

        init();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return serviceInjector;
    }

    private void init() {

        // To handle RxJava exceptions
        RxJavaPlugins.setErrorHandler(new DefaultRxErrorHandler(analytics));

        flex.initialize();
        userPreferenceManager.initialize();
        orderingPreferencesManager.initialize();
        onLaunchDataPreFetcher.loadUserData();
        identityManager.initialize();
        pushManager.initialize();
        purchaseManager.initialize(this);
        cognitoManager.initialize();
        ocrManager.initialize();
        crashReporter.initialize();
        receiptsOrderer.initialize();
        markedForDeletionCleaner.safelyDeleteAllOutstandingItems();
        picassoInitializer.initialize();

        PDFBoxResourceLoader.init(getApplicationContext());

        // Clear our cache
        Completable.fromAction(() -> {
                    new SmartReceiptsTemporaryFileCache(this).resetCache();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();

        // Check if a new version is available
        new AppVersionManager(this, persistenceManager.getPreferenceManager()).onLaunch(this);

        // Add launch count for rating prompt monitoring
        appRatingPreferencesStorage.incrementLaunchCount();

        // LeakCanary initialization
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
        } else {
            LeakCanary.install(this);
        }

        extraInitializer.init();
    }

    /**
     * Called when upgrading our application version. We currently have the following rules in place:
     * <ul>
     * <li>From 78: We migrate the database from internal to external storage to assist with data
     * recovery if a user breaks his/her screen (or equivalent).</li>
     * </ul>
     *
     * @param oldVersion the old application version
     * @param newVersion the new application version
     */
    @Override
    public void onVersionUpgrade(int oldVersion, int newVersion) {
        Logger.debug(this, "Upgrading the app from version {} to {}", oldVersion, newVersion);
        if (oldVersion <= 78) {
            try {
                StorageManager external = persistenceManager.getExternalStorageManager();
                File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); // Internal db file
                if (db != null && db.exists()) {
                    File sdDB = external.getFile("receipts.db");
                    Logger.debug(this, "Copying the database file from {} to {}", db.getAbsolutePath(), sdDB.getAbsolutePath());
                    try {
                        external.copy(db, sdDB, true);
                    } catch (IOException e) {
                        Logger.error(this, "Exception occurred when upgrading app version", e);
                    }
                }
            } catch (SDCardStateException e) {
                Logger.warn(this, "Caught sd card exception", e);
            }
        }
    }

}
