package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum InAppPurchase {

    // Note: Smart Receipts Plus users also get some free OCR scans
    SmartReceiptsPlus(Subscription.class, "plus_sku_4", new HashSet<>(Arrays.asList("pro_sku_3", "plus_sku_5")), new HashSet<>(Arrays.asList(PurchaseFamily.SmartReceiptsPlus, PurchaseFamily.Ocr))),
    OcrScans10(ConsumablePurchase.class, "ocr_purchase_10", PurchaseFamily.Ocr),
    OcrScans50(ConsumablePurchase.class, "ocr_purchase_1", PurchaseFamily.Ocr),

    /**
     * A test only {@link ConsumablePurchase} for testing without a particular {@link PurchaseFamily}
     */
    @VisibleForTesting
    TestConsumablePurchase(ConsumablePurchase.class, "test_consumable_purchase", Collections.emptySet()),

    /**
     * A test only {@link Subscription} for testing without a particular {@link PurchaseFamily}
     */
    @VisibleForTesting
    TestSubscription(Subscription.class, "test_subscription", Collections.singleton("test_legacy_subscription"), Collections.emptySet());

    private final Class<? extends ManagedProduct> type;
    private final String sku;
    private final Set<String> legacySkus;
    private final Set<PurchaseFamily> purchaseFamilies;

    InAppPurchase(@NonNull Class<? extends ManagedProduct> type,
                  @NonNull String sku,
                  @NonNull PurchaseFamily purchaseFamily) {
        this(type, sku, Collections.singleton(purchaseFamily));
    }

    InAppPurchase(@NonNull Class<? extends ManagedProduct> type,
                  @NonNull String sku,
                  @NonNull Set<PurchaseFamily> purchaseFamilies) {
        this(type, sku, Collections.emptySet(), purchaseFamilies);
    }

    InAppPurchase(@NonNull Class<? extends ManagedProduct> type,
                  @NonNull String sku,
                  @NonNull Set<String> legacySkus,
                  @NonNull Set<PurchaseFamily> purchaseFamilies) {
        this.type = Preconditions.checkNotNull(type);
        this.sku = Preconditions.checkNotNull(sku);
        this.legacySkus = Preconditions.checkNotNull(legacySkus);
        this.purchaseFamilies = Preconditions.checkNotNull(purchaseFamilies);
    }

    /**
     * @return the unique {@link String} identifier (ie stock keeping unit) for this product
     */
    @NonNull
    public String getSku() {
        return sku;
    }

    /**
     * For subscriptions, Google does not allow for price changes to occur. To better handle this,
     * we allow "legacy" skus (ie stock keeping unit) to still properly map to our purchase type.
     * Callers of this method will receive a full set of legacy skus that correspond to historical
     * pricing paradigms
     *
     * @return a {@link Set} of {@link String} identifiers for this product.
     */
    @NonNull
    public Set<String> getLegacySkus() {
        return legacySkus;
    }

    /**
     * @return the type of {@link ManagedProduct} that this is
     */
    @NonNull
    public Class<? extends ManagedProduct> getType() {
        return type;
    }

    /**
     * @return the {@link Set} of all {@link PurchaseFamily} that are supported for this purchase type
     */
    @NonNull
    public Set<PurchaseFamily> getPurchaseFamilies() {
        return purchaseFamilies;
    }

    /**
     * @return the {@link String} of the Google product type (ie "inapp" or "subs")
     */
    @NonNull
    public String getProductType() {
        if (ConsumablePurchase.class.equals(type)) {
            return ConsumablePurchase.GOOGLE_PRODUCT_TYPE;
        } else {
            return Subscription.GOOGLE_PRODUCT_TYPE;
        }
    }

    @Nullable
    public static InAppPurchase from(@Nullable String sku) {
        for (final InAppPurchase inAppPurchase : values()) {
            if (inAppPurchase.getSku().equals(sku)) {
                return inAppPurchase;
            }
            for (final String legacySku : inAppPurchase.getLegacySkus()) {
                if (legacySku.equals(sku)) {
                    return inAppPurchase;
                }
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<String> getConsumablePurchaseSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            if (ConsumablePurchase.class.equals(inAppPurchase.getType()) && inAppPurchase != TestConsumablePurchase) {
                skus.add(inAppPurchase.getSku());
            }
        }
        return skus;
    }

    @NonNull
    public static ArrayList<String> getSubscriptionSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            if (Subscription.class.equals(inAppPurchase.getType())  && inAppPurchase != TestSubscription) {
                skus.add(inAppPurchase.getSku());

            }
        }
        return skus;
    }
}