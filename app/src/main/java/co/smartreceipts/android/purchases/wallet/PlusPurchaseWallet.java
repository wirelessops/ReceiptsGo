package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import dagger.Lazy;

public final class PlusPurchaseWallet extends DefaultPurchaseWallet {

    @Inject
    public PlusPurchaseWallet(@NonNull Lazy<SharedPreferences> preferences) {
        super(preferences);
    }

    @Override
    public synchronized boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        if (inAppPurchase == InAppPurchase.SmartReceiptsPlus) {
            return true;
        } else {
            return super.hasActivePurchase(inAppPurchase);
        }
    }

}
