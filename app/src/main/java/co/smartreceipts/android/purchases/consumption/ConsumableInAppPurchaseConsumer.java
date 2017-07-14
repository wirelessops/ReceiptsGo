package co.smartreceipts.android.purchases.consumption;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import io.reactivex.Completable;

class ConsumableInAppPurchaseConsumer implements InAppPurchaseConsumer<ConsumablePurchase> {

    private final PurchaseManager purchaseManager;

    @Inject
    public ConsumableInAppPurchaseConsumer(@NonNull PurchaseManager purchaseManager) {
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
    }

    @Override
    public boolean isConsumed(@NonNull ConsumablePurchase managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        return false;
    }

    @Override
    public Completable consumePurchase(@NonNull ConsumablePurchase managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        return purchaseManager.consumePurchase(managedProduct);
    }
}
