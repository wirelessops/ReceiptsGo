package co.smartreceipts.android;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
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
import io.reactivex.plugins.RxJavaPlugins;
import wb.android.BuildConfig;
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
    ExtraInitializer extraInitializer;

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


    private AppComponent appComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configureLog();

        if (BuildConfig.DEBUG) {
            Logger.debug(this, "Enabling strict mode");
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        appComponent = DaggerAppComponent.builder()
                .baseAppModule(new BaseAppModule(this))
                .build();

        appComponent.inject(this);

        WBUncaughtExceptionHandler.initialize();

        Logger.debug(this, "\n\n\n\n Launching App...");

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
        pushManager.initialize();
        purchaseManager.initialize(this);
        cognitoManager.initialize();
        ocrManager.initialize();
        orderingPreferencesManager.initialize();
        crashReporter.initialize();
        receiptsOrderer.initialize();
        markedForDeletionCleaner.safelyDeleteAllOutstandingItems();

        PDFBoxResourceLoader.init(getApplicationContext());

        // Clear our cache
        new SmartReceiptsTemporaryFileCache(this).resetCache();

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

    private void configureLog() {
        final String logDirPath = getFilesDir().getPath();
        System.setProperty("LOG_DIR", logDirPath);
    }

    // This is called after _sdCard is available but before _db is
    // This was added after version 78 (version 79 is the first "new" one)
    // Make this a listener
    @Override
    public void onVersionUpgrade(int oldVersion, int newVersion) {
        Logger.debug(this, "Upgrading the app from version {} to {}", oldVersion, newVersion);
        if (oldVersion <= 78) {
            try {
                StorageManager external = persistenceManager.getExternalStorageManager();
                File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); // Internal db file
                if (db != null && db.exists()) {
                    File sdDB = external.getFile("receipts.db");
                    if (sdDB.exists()) {
                        sdDB.delete();
                    }
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
