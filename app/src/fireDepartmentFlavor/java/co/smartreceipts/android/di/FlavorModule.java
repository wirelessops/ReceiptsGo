package co.smartreceipts.android.di;

import android.content.Context;

import javax.inject.Named;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.AnalyticsProvider;
import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFireDepartmentImpl;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.ocr.OcrManagerImpl;
import co.smartreceipts.android.purchases.wallet.PlusPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.GoogleDriveBackupManager;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManagerImpl;
import co.smartreceipts.android.sync.provider.SyncProviderFactory;
import co.smartreceipts.aws.cognito.CognitoManager;
import co.smartreceipts.aws.cognito.CognitoManagerImpl;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.push.PushManager;
import co.smartreceipts.push.PushManagerImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class FlavorModule {

    @Binds
    @ApplicationScope
    public abstract PurchaseWallet providePurchaseWallet(PlusPurchaseWallet plusPurchaseWallet);

    @Binds
    @ApplicationScope
    public abstract ExtraInitializer provideExtraInitializer(ExtraInitializerFireDepartmentImpl initializer);

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager, Context context) {
        return new AnalyticsManager(new AnalyticsProvider(context).getAnalytics(), userPreferenceManager);
    }

    @Binds
    @ApplicationScope
    public abstract OcrManager provideOcrManager(OcrManagerImpl ocrManager);

    @Binds
    @ApplicationScope
    public abstract CognitoManager provideCognitoManager(CognitoManagerImpl cognitoManager);

    @Binds
    @ApplicationScope
    public abstract PushManager providePushManager(PushManagerImpl pushManager);

    @Binds
    @ApplicationScope
    @Named(SyncProviderFactory.DRIVE_BACKUP_MANAGER)
    public abstract BackupProvider provideDriveBackupManager(GoogleDriveBackupManager googleDriveBackupManager);

    @Binds
    @ApplicationScope
    public abstract GoogleDriveTableManager provideGoogleDriveTableManager(GoogleDriveTableManagerImpl driveTableManager);

}
