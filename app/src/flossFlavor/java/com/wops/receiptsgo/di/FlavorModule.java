package com.wops.receiptsgo.di;

import javax.inject.Named;

import com.wops.analytics.Analytics;
import com.wops.analytics.AnalyticsProvider;
import com.wops.receiptsgo.ExtraInitializer;
import com.wops.receiptsgo.ExtraInitializerFlossImpl;
import com.wops.receiptsgo.ad.MobileAds;
import com.wops.receiptsgo.ad.NoMobileAds;
import com.wops.receiptsgo.analytics.AnalyticsManager;
import com.wops.receiptsgo.ocr.NoOpOcrManager;
import com.wops.receiptsgo.ocr.OcrManager;
import com.wops.receiptsgo.purchases.wallet.DefaultPurchaseWallet;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.sync.BackupProvider;
import com.wops.receiptsgo.sync.drive.managers.GoogleDriveTableManager;
import com.wops.receiptsgo.sync.drive.managers.NoOpGoogleDriveTableManager;
import com.wops.receiptsgo.sync.noop.NoOpBackupProvider;
import com.wops.receiptsgo.sync.provider.SyncProviderFactory;
import com.wops.aws.cognito.CognitoManager;
import com.wops.aws.cognito.NoOpCognitoManager;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.push.NoOpPushManager;
import com.wops.push.PushManager;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class FlavorModule {

    @Binds
    @ApplicationScope
    public abstract PurchaseWallet providePurchaseWallet(DefaultPurchaseWallet defaultPurchaseWallet);

    @Binds
    @ApplicationScope
    public abstract ExtraInitializer provideExtraInitializer(ExtraInitializerFlossImpl flossInitializer);

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager) {
        return new AnalyticsManager(new AnalyticsProvider().getAnalytics(), userPreferenceManager);
    }

    @Binds
    @ApplicationScope
    public abstract OcrManager provideOcrManager(NoOpOcrManager ocrManager);

    @Binds
    @ApplicationScope
    public abstract CognitoManager provideCognitoManager(NoOpCognitoManager cognitoManager);

    @Binds
    @ApplicationScope
    public abstract PushManager providePushManager(NoOpPushManager pushManager);

    @Binds
    @ApplicationScope
    @Named(SyncProviderFactory.DRIVE_BACKUP_MANAGER)
    public abstract BackupProvider provideDriveBackupManager(NoOpBackupProvider noOpBackupManager);

    @Binds
    @ApplicationScope
    public abstract GoogleDriveTableManager provideGoogleDriveTableManager(NoOpGoogleDriveTableManager driveTableManager);

    @Binds
    @ApplicationScope
    public abstract MobileAds provideMobileAds(NoMobileAds mobileAds);
}
