package co.smartreceipts.android.purchases.consumption;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import co.smartreceipts.android.purchases.model.Subscription;
import dagger.Lazy;
import io.reactivex.Completable;

@ApplicationScope
public class SubscriptionInAppPurchaseConsumer implements InAppPurchaseConsumer<Subscription> {

    private static final String KEY_CONSUMED_SUBSCRIPTION_SET = "key_consumed_subscription_set";
    private static final String FORMAT_KEY_PURCHASE_FAMILY = "key_%s_purchase_family";

    private final Lazy<SharedPreferences> sharedPreferences;

    @Inject
    public SubscriptionInAppPurchaseConsumer(@NonNull Lazy<SharedPreferences> preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
    }

    @Override
    public boolean isConsumed(@NonNull Subscription managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        final Set<String> consumedSubscriptionSkuSet = sharedPreferences.get().getStringSet(KEY_CONSUMED_SUBSCRIPTION_SET, Collections.emptySet());
        for (final String sku : consumedSubscriptionSkuSet) {
            if (managedProduct.getInAppPurchase().equals(InAppPurchase.from(sku))) {
                final String family = sharedPreferences.get().getString(getPurchaseFamilyKey(sku), "");
                if (purchaseFamily.name().equals(family)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Completable consumePurchase(@NonNull Subscription managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        return Completable.fromAction(() -> {
            final Set<String> consumedSubscriptionSkuSet = new HashSet<>(sharedPreferences.get().getStringSet(KEY_CONSUMED_SUBSCRIPTION_SET, Collections.emptySet()));
            final String sku = managedProduct.getInAppPurchase().getSku();
            if (!consumedSubscriptionSkuSet.contains(sku)) {
                consumedSubscriptionSkuSet.add(sku);
                final SharedPreferences.Editor editor = sharedPreferences.get().edit();
                editor.putStringSet(KEY_CONSUMED_SUBSCRIPTION_SET, consumedSubscriptionSkuSet);
                editor.putString(getPurchaseFamilyKey(sku), purchaseFamily.name());
                editor.apply();
            }
        });
    }

    @NonNull
    private String getPurchaseFamilyKey(@NonNull String sku) {
        return String.format(Locale.US, FORMAT_KEY_PURCHASE_FAMILY, sku);
    }
}
