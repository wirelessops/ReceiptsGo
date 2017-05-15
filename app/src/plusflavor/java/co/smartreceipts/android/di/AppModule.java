package co.smartreceipts.android.di;

import java.util.Arrays;
import java.util.Collections;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerPlusImpl;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.impl.firebase.FirebaseAnalytics;
import co.smartreceipts.android.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.PlusPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @ApplicationScope
    public static PurchaseWallet providePurchaseWallet (PlusPurchaseWallet plusPurchaseWallet) {
        return plusPurchaseWallet;
    }

    @Provides
    @ApplicationScope
    public static ExtraInitializer provideExtraInitializer (ExtraInitializerPlusImpl plusInitializer) {
        return plusInitializer;
    }

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(FirebaseAnalytics firebaseAnalytics) {
        return new AnalyticsManager(Collections.unmodifiableList(Arrays.asList(new AnalyticsLogger(),
                firebaseAnalytics)));
    }
}
