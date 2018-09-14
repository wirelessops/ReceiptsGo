package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.ManagedProductFactory;
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription;
import co.smartreceipts.android.utils.log.Logger;
import dagger.Lazy;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private static final String KEY_SKU_SET = "key_sku_set";
    private static final String FORMAT_KEY_PURCHASE_DATA = "%s_purchaseData";
    private static final String FORMAT_KEY_IN_APP_DATA_SIGNATURE = "%s_inAppDataSignature";

    private final Lazy<SharedPreferences> sharedPreferences;
    private Map<InAppPurchase, ManagedProduct> ownedInAppPurchasesMap = null;
    private Map<InAppPurchase, RemoteSubscription> ownedRemotePurchasesMap = Collections.emptyMap();

    @Inject
    public DefaultPurchaseWallet(@NonNull Lazy<SharedPreferences> preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
    }

    @Override
    public synchronized boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        return getOwnedInAppPurchasesMap().containsKey(inAppPurchase) || ownedRemotePurchasesMap.containsKey(inAppPurchase);
    }

    @NonNull
    @Override
    public Set<ManagedProduct> getActiveLocalInAppPurchases() {
        return new HashSet<>(getOwnedInAppPurchasesMap().values());
    }

    @Nullable
    @Override
    public synchronized ManagedProduct getLocalInAppManagedProduct(@NonNull InAppPurchase inAppPurchase) {
        return getOwnedInAppPurchasesMap().get(inAppPurchase);
    }

    @Override
    public synchronized void updateLocalInAppPurchasesInWallet(@NonNull Set<ManagedProduct> managedProducts) {
        final Map<InAppPurchase, ManagedProduct> actualInAppPurchasesMap = new HashMap<>();
        for (final ManagedProduct managedProduct : managedProducts) {
            actualInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
        }

        getOwnedInAppPurchasesMap();
        if (!actualInAppPurchasesMap.equals(ownedInAppPurchasesMap)) {
            // Only update if we actually added something to the underlying set
            ownedInAppPurchasesMap.clear();
            ownedInAppPurchasesMap.putAll(actualInAppPurchasesMap);
            persistWallet();
        }
    }

    @Override
    public synchronized void updateRemotePurchases(@NonNull Set<RemoteSubscription> remoteSubscriptions) {
        final Map<InAppPurchase, RemoteSubscription> localOwnedRemotePurchasesMap = new HashMap<>();
        for (final RemoteSubscription remoteSubscription : remoteSubscriptions) {
            localOwnedRemotePurchasesMap.put(remoteSubscription.getInAppPurchase(), remoteSubscription);
        }

        // Note: We just keep this in memory as we trust that our OkHttp cache handles this
        this.ownedRemotePurchasesMap = localOwnedRemotePurchasesMap;
    }

    @Override
    public synchronized void addLocalInAppPurchaseToWallet(@NonNull ManagedProduct managedProduct) {
        if (!getOwnedInAppPurchasesMap().containsKey(managedProduct.getInAppPurchase())) {
            ownedInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
            persistWallet();
        }
    }

    @NonNull
    private synchronized Map<InAppPurchase, ManagedProduct> getOwnedInAppPurchasesMap() {
        if (this.ownedInAppPurchasesMap == null) {
            final Set<String> skusSet = sharedPreferences.get().getStringSet(KEY_SKU_SET, Collections.emptySet());
            final Map<InAppPurchase, ManagedProduct> inAppPurchasesMap = new HashMap<>();
            for (final String sku : skusSet) {
                final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                if (inAppPurchase != null) {
                    final String purchaseData = sharedPreferences.get().getString(getKeyForPurchaseData(inAppPurchase), "");
                    final String inAppDataSignature = sharedPreferences.get().getString(getKeyForInAppDataSignature(inAppPurchase), "");
                    try {
                        final ManagedProduct managedProduct = new ManagedProductFactory(inAppPurchase, purchaseData, inAppDataSignature).get();
                        inAppPurchasesMap.put(inAppPurchase, managedProduct);
                    } catch (JSONException e) {
                        Logger.error(this, "Failed to parse the purchase data for " + inAppPurchase, e);
                    }
                }
            }
            this.ownedInAppPurchasesMap = inAppPurchasesMap;
        }
        return this.ownedInAppPurchasesMap;
    }

    private void persistWallet() {
        final Set<InAppPurchase> ownedInAppPurchases = new HashSet<>(ownedInAppPurchasesMap.keySet());
        final Set<String> skusSet = new HashSet<>();
        final SharedPreferences.Editor editor = sharedPreferences.get().edit();

        // Note per: https://developer.android.com/reference/android/content/SharedPreferences.Editor.html#remove(java.lang.String)
        // All removals are done first, regardless of whether you called remove before or after put methods on this editor.
        for (final InAppPurchase inAppPurchase : InAppPurchase.values()) {
            editor.remove(getKeyForPurchaseData(inAppPurchase));
            editor.remove(getKeyForInAppDataSignature(inAppPurchase));
        }

        for (final InAppPurchase inAppPurchase : ownedInAppPurchases) {
            final ManagedProduct managedProduct = ownedInAppPurchasesMap.get(inAppPurchase);
            skusSet.add(inAppPurchase.getSku());
            editor.putString(getKeyForPurchaseData(inAppPurchase), managedProduct.getPurchaseData());
            editor.putString(getKeyForInAppDataSignature(inAppPurchase), managedProduct.getInAppDataSignature());
        }
        editor.putStringSet(KEY_SKU_SET, skusSet);
        editor.apply();
    }

    @NonNull
    private String getKeyForPurchaseData(@NonNull InAppPurchase inAppPurchase) {
        return String.format(Locale.US, FORMAT_KEY_PURCHASE_DATA, inAppPurchase.getSku());
    }

    @NonNull
    private String getKeyForInAppDataSignature(@NonNull InAppPurchase inAppPurchase) {
        return String.format(Locale.US, FORMAT_KEY_IN_APP_DATA_SIGNATURE, inAppPurchase.getSku());
    }

}
