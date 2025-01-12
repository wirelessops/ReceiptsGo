package com.wops.receiptsgo.purchases.wallet;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.model.ManagedProduct;
import com.wops.receiptsgo.purchases.model.ManagedProductFactory;
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription;
import com.wops.analytics.log.Logger;
import dagger.Lazy;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private static final String KEY_SKU_SET = "key_sku_set";
    private static final String FORMAT_KEY_PURCHASE_DATA = "%s_purchaseData";
    private static final String FORMAT_KEY_IN_APP_DATA_SIGNATURE = "%s_inAppDataSignature";
    private static final String KEY_REMOTE_SKU_SET = "key_remote_purchase_sku_set";

    private final Lazy<SharedPreferences> sharedPreferences;
    private Map<InAppPurchase, ManagedProduct> locallyOwnedInAppPurchasesMap = null;
    private Set<InAppPurchase> remotelyOwnedPurchases = null;

    @Inject
    public DefaultPurchaseWallet(@NonNull Lazy<SharedPreferences> preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
    }

    @Override
    public synchronized boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        return getLocallyOwnedInAppPurchasesMap().containsKey(inAppPurchase) || getRemotelyOwnedPurchases().contains(inAppPurchase);
    }

    @NonNull
    @Override
    public Set<ManagedProduct> getActiveLocalInAppPurchases() {
        return new HashSet<>(getLocallyOwnedInAppPurchasesMap().values());
    }

    @Nullable
    @Override
    public synchronized ManagedProduct getLocalInAppManagedProduct(@NonNull InAppPurchase inAppPurchase) {
        return getLocallyOwnedInAppPurchasesMap().get(inAppPurchase);
    }

    @Override
    public synchronized void updateLocalInAppPurchasesInWallet(@NonNull Set<ManagedProduct> managedProducts) {
        final Map<InAppPurchase, ManagedProduct> actualInAppPurchasesMap = new HashMap<>();
        for (final ManagedProduct managedProduct : managedProducts) {
            actualInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
        }

        getLocallyOwnedInAppPurchasesMap();
        if (!actualInAppPurchasesMap.equals(locallyOwnedInAppPurchasesMap)) {
            // Only update if we actually added something to the underlying set
            locallyOwnedInAppPurchasesMap.clear();
            locallyOwnedInAppPurchasesMap.putAll(actualInAppPurchasesMap);
            persistWallet();
        }
    }

    @Override
    public synchronized void updateRemotePurchases(@NonNull Set<RemoteSubscription> remoteSubscriptions) {
        final Set<InAppPurchase> remotePurchases = new HashSet<>();
        for (final RemoteSubscription remoteSubscription : remoteSubscriptions) {
            remotePurchases.add(remoteSubscription.getInAppPurchase());
        }
        if (!getRemotelyOwnedPurchases().equals(remotePurchases)) {
            this.remotelyOwnedPurchases = remotePurchases;
            persistWallet();
        }
    }

    @Override
    public synchronized void addLocalInAppPurchaseToWallet(@NonNull ManagedProduct managedProduct) {
        if (!getLocallyOwnedInAppPurchasesMap().containsKey(managedProduct.getInAppPurchase())) {
            locallyOwnedInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
            persistWallet();
        }
    }

    @NonNull
    private synchronized Map<InAppPurchase, ManagedProduct> getLocallyOwnedInAppPurchasesMap() {
        if (this.locallyOwnedInAppPurchasesMap == null) {
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
            this.locallyOwnedInAppPurchasesMap = inAppPurchasesMap;
        }
        return this.locallyOwnedInAppPurchasesMap;
    }

    @NonNull
    private synchronized Set<InAppPurchase> getRemotelyOwnedPurchases() {
        if (this.remotelyOwnedPurchases == null) {
            final Set<String> skusSet = sharedPreferences.get().getStringSet(KEY_REMOTE_SKU_SET, Collections.emptySet());
            final Set<InAppPurchase> remotePurchaseSet = new HashSet<>();
            for (final String sku : skusSet) {
                final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                if (inAppPurchase != null) {
                    remotePurchaseSet.add(inAppPurchase);
                }
            }
            this.remotelyOwnedPurchases = remotePurchaseSet;
        }
        return this.remotelyOwnedPurchases;
    }

    private void persistWallet() {
        final SharedPreferences.Editor editor = sharedPreferences.get().edit();

        if (locallyOwnedInAppPurchasesMap != null) {
            final Set<InAppPurchase> ownedInAppPurchases = new HashSet<>(locallyOwnedInAppPurchasesMap.keySet());
            final Set<String> skusSet = new HashSet<>();

            // Note per: https://developer.android.com/reference/android/content/SharedPreferences.Editor.html#remove(java.lang.String)
            // All removals are done first, regardless of whether you called remove before or after put methods on this editor.
            for (final InAppPurchase inAppPurchase : InAppPurchase.values()) {
                editor.remove(getKeyForPurchaseData(inAppPurchase));
                editor.remove(getKeyForInAppDataSignature(inAppPurchase));
            }

            for (final InAppPurchase inAppPurchase : ownedInAppPurchases) {
                final ManagedProduct managedProduct = locallyOwnedInAppPurchasesMap.get(inAppPurchase);
                skusSet.add(inAppPurchase.getSku());
                editor.putString(getKeyForPurchaseData(inAppPurchase), managedProduct.getPurchaseDataJson());
                editor.putString(getKeyForInAppDataSignature(inAppPurchase), managedProduct.getInAppDataSignature());
            }

            editor.putStringSet(KEY_SKU_SET, skusSet);
        }

        if (remotelyOwnedPurchases != null) {
            final Set<String> remoteSkusSet = new HashSet<>();
            for (final InAppPurchase remoteInAppPurchase : remotelyOwnedPurchases) {
                remoteSkusSet.add(remoteInAppPurchase.getSku());
            }
            editor.putStringSet(KEY_REMOTE_SKU_SET, remoteSkusSet);
        }

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
