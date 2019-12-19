package co.smartreceipts.android.di;

import java.util.Arrays;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFireDepartmentImpl;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.impl.firebase.FirebaseAnalytics;
import co.smartreceipts.android.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.ocr.OcrManagerImpl;
import co.smartreceipts.android.purchases.wallet.PlusPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.aws.cognito.CognitoManager;
import co.smartreceipts.aws.cognito.CognitoManagerImpl;
import co.smartreceipts.core.di.scopes.ApplicationScope;
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
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager, FirebaseAnalytics firebaseAnalytics) {
        return new AnalyticsManager(Arrays.asList(new AnalyticsLogger(), firebaseAnalytics), userPreferenceManager);
    }

    @Binds
    @ApplicationScope
    public abstract OcrManager provideOcrManager(OcrManagerImpl ocrManager);

    @Binds
    @ApplicationScope
    public abstract CognitoManager provideCognitoManager(CognitoManagerImpl cognitoManager);
}
