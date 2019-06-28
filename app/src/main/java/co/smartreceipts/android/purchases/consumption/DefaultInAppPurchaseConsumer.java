package co.smartreceipts.android.purchases.consumption;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import co.smartreceipts.android.purchases.model.Subscription;
import io.reactivex.Completable;

@ApplicationScope
public class DefaultInAppPurchaseConsumer implements InAppPurchaseConsumer<ManagedProduct> {

    private final ConsumableInAppPurchaseConsumer consumableInAppPurchaseConsumer;
    private final SubscriptionInAppPurchaseConsumer subscriptionInAppPurchaseConsumer;

    @Inject
    public DefaultInAppPurchaseConsumer(@NonNull ConsumableInAppPurchaseConsumer consumableInAppPurchaseConsumer,
                                        @NonNull SubscriptionInAppPurchaseConsumer subscriptionInAppPurchaseConsumer) {
        this.consumableInAppPurchaseConsumer = Preconditions.checkNotNull(consumableInAppPurchaseConsumer);
        this.subscriptionInAppPurchaseConsumer = Preconditions.checkNotNull(subscriptionInAppPurchaseConsumer);
    }

    @Override
    public boolean isConsumed(@NonNull ManagedProduct managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        if (managedProduct instanceof ConsumablePurchase) {
            return consumableInAppPurchaseConsumer.isConsumed((ConsumablePurchase)managedProduct, purchaseFamily);
        } else if (managedProduct instanceof Subscription) {
            return subscriptionInAppPurchaseConsumer.isConsumed((Subscription)managedProduct, purchaseFamily);
        } else {
            throw new IllegalArgumentException("Unsupported managed product type: " + managedProduct);
        }
    }

    @Override
    public Completable consumePurchase(@NonNull ManagedProduct managedProduct, @NonNull PurchaseFamily purchaseFamily) {
        if (managedProduct instanceof ConsumablePurchase) {
            return consumableInAppPurchaseConsumer.consumePurchase((ConsumablePurchase)managedProduct, purchaseFamily);
        } else if (managedProduct instanceof Subscription) {
            return subscriptionInAppPurchaseConsumer.consumePurchase((Subscription)managedProduct, purchaseFamily);
        } else {
            throw new IllegalArgumentException("Unsupported managed product type: " + managedProduct);
        }
    }
}
