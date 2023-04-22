package co.smartreceipts.android.di;

import javax.inject.Named;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.AnalyticsProvider;
import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFlossImpl;
import co.smartreceipts.android.ad.MobileAds;
import co.smartreceipts.android.ad.NoMobileAds;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.ocr.NoOpOcrManager;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager;
import co.smartreceipts.android.sync.drive.managers.NoOpGoogleDriveTableManager;
import co.smartreceipts.android.sync.noop.NoOpBackupProvider;
import co.smartreceipts.android.sync.provider.SyncProviderFactory;
import co.smartreceipts.aws.cognito.CognitoManager;
import co.smartreceipts.aws.cognito.NoOpCognitoManager;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.push.NoOpPushManager;
import co.smartreceipts.push.PushManager;
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
