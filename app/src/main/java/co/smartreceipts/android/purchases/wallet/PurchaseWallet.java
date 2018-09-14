package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription;

public interface PurchaseWallet {

    /**
     * Checks if this user owns a particular {@link InAppPurchase} for this application
     *
     * @param inAppPurchase the purchase to check for
     * @return {@code true} if it's both owned and active. {@code false} otherwise
     */
    boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase);

    /**
     * Gets complete {@link Set} of all actively owned "in-app" purchases. Please note that this
     * differs from the {@link #hasActivePurchase(InAppPurchase)} method, since this ONLY checks
     * for local purchases. In cases in which we purchased the plus version as a standalone app or
     * via another platform (e.g. Apple or Stripe), the result of this method will not include that
     *
     * @return a complete {@link Set} of all actively owned "in-app" purchases.
     */
    @NonNull
    Set<ManagedProduct> getActiveLocalInAppPurchases();

    /**
     * Fetches the {@link ManagedProduct} that is associated with a particular {@link InAppPurchase}
     *
     * @param inAppPurchase - the {@link InAppPurchase} to look for
     * @return the corresponding {@link ManagedProduct} of {@code null} if this item is either
     * unowned or was not purchased via Google Play Services
     */
    @Nullable
    ManagedProduct getLocalInAppManagedProduct(@NonNull InAppPurchase inAppPurchase);

    /**
     * Adds a new purchase to our existing wallet
     *
     * @param managedProduct the {@link ManagedProduct} to add to our wallet
     */
    void addLocalInAppPurchaseToWallet(@NonNull ManagedProduct managedProduct);

    /**
     * Updates the list of purchased products that are owned in this wallet
     *
     * @param managedProducts the {@link Set} of {@link ManagedProduct}s that are owned by this wallet
     */
    void updateLocalInAppPurchasesInWallet(@NonNull Set<ManagedProduct> managedProducts);

    /**
     * Updates the list of owned remote purchases in this wallet
     *
     * @param remoteSubscriptions the {@link Set} of {@link RemoteSubscription}s that are owned by this wallet
     */
    void updateRemotePurchases(@NonNull Set<RemoteSubscription> remoteSubscriptions);

}
