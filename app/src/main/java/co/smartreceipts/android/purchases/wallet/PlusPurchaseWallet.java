package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.ManagedProductFactory;
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
