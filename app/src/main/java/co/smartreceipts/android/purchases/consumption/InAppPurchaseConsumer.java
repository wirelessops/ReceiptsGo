package co.smartreceipts.android.purchases.consumption;

import androidx.annotation.NonNull;

import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import io.reactivex.Completable;

public interface InAppPurchaseConsumer<T extends ManagedProduct> {

    /**
     * Checks if a particular {@link T} was consumed for a specific {@link PurchaseFamily}
     *
     * @param managedProduct the {@link T} to check
     * @param purchaseFamily the {@link PurchaseFamily} that we're checking for
     *
     * @return {@code true} if it is consumed, or {@code false} if not
     */
    boolean isConsumed(@NonNull T managedProduct, @NonNull PurchaseFamily purchaseFamily);

    /**
     * Consumes a purchase for a particular {@link T} was consumed for a specific
     * {@link PurchaseFamily}
     *
     * @param managedProduct the {@link T} to check
     * @param purchaseFamily the {@link PurchaseFamily} that we're checking for
     *
     * @return a {@link Completable} that will complete if this consumption is handled properly or
     * an error if not
     */
    Completable consumePurchase(@NonNull T managedProduct, @NonNull PurchaseFamily purchaseFamily);
}
