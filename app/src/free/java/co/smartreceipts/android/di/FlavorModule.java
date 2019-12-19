package co.smartreceipts.android.di;

import android.content.Context;

import com.google.android.gms.analytics.Tracker;

import java.util.Arrays;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFreeImpl;
import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.GoogleAnalytics;
import co.smartreceipts.android.analytics.impl.firebase.FirebaseAnalytics;
import co.smartreceipts.android.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.ocr.OcrManagerImpl;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.aws.cognito.CognitoManager;
import co.smartreceipts.aws.cognito.CognitoManagerImpl;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class FlavorModule {

    @Provides
    @ApplicationScope
    public static PurchaseWallet providePurchaseWallet(DefaultPurchaseWallet defaultPurchaseWallet) {
        return defaultPurchaseWallet;
    }

    @Provides
    @ApplicationScope
    public static ExtraInitializer provideExtraInitializer(ExtraInitializerFreeImpl freeInitializer) {
        return freeInitializer;
    }

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager, FirebaseAnalytics firebaseAnalytics, GoogleAnalytics googleAnalytics) {
        return new AnalyticsManager(Arrays.asList(new AnalyticsLogger(), firebaseAnalytics, googleAnalytics), userPreferenceManager);
    }

    @Provides
    @ApplicationScope
    public static Tracker provideGoogleAnalyticsTracker(Context context) {
        return com.google.android.gms.analytics.GoogleAnalytics.getInstance(context).newTracker(R.xml.analytics);
    }

    @Provides
    @ApplicationScope
    public static OcrManager provideOcrManager(OcrManagerImpl ocrManager) {
        return ocrManager;
    }

    @Provides
    @ApplicationScope
    public static CognitoManager provideCognitoManager(CognitoManagerImpl cognitoManager) {
        return cognitoManager;
    }
}
