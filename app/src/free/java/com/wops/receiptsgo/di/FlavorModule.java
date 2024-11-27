package com.wops.receiptsgo.di;

import android.content.Context;



import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.wops.analytics.Analytics;
import com.wops.analytics.AnalyticsProvider;
import com.wops.receiptsgo.ExtraInitializer;
import com.wops.receiptsgo.ExtraInitializerFreeImpl;

import com.wops.receiptsgo.ad.MobileAds;
import com.wops.receiptsgo.ad.MobileAdsImpl;
import com.wops.receiptsgo.analytics.AnalyticsManager;

import com.wops.receiptsgo.ocr.OcrManager;
import com.wops.receiptsgo.ocr.OcrManagerImpl;
import com.wops.receiptsgo.purchases.wallet.DefaultPurchaseWallet;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.sync.BackupProvider;
import com.wops.receiptsgo.sync.drive.GoogleDriveBackupManager;
import com.wops.receiptsgo.sync.drive.managers.GoogleDriveTableManager;
import com.wops.receiptsgo.sync.drive.managers.GoogleDriveTableManagerImpl;
import com.wops.receiptsgo.sync.provider.SyncProviderFactory;
import com.wops.aws.cognito.CognitoManager;
import com.wops.aws.cognito.CognitoManagerImpl;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.push.PushManager;
import com.wops.push.PushManagerImpl;
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
    public abstract ExtraInitializer provideExtraInitializer(ExtraInitializerFreeImpl freeInitializer);

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager, Context context) {
        final List<Analytics> defaultAnalytics = new ArrayList<>(new AnalyticsProvider(context).getAnalytics());
        return new AnalyticsManager(defaultAnalytics, userPreferenceManager);
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

    @Binds
    @ApplicationScope
    public abstract MobileAds provideMobileAds(MobileAdsImpl mobileAds);

}
