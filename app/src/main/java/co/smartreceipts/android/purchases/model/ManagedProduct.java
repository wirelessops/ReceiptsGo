package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;

public interface ManagedProduct {

    /**
     * @return the {@link InAppPurchase} enum that keys this managed product type
     */
    @NonNull
    InAppPurchase getInAppPurchase();

    /**
     * @return the {@link String} purchase token provided by Google for this product
     */
    @NonNull
    String getPurchaseToken();

    /**
     * @return the purchase data json {@link String} provided by Google for this purchase
     */
    @NonNull
    String getPurchaseData();

    /**
     * @return the {@link String} data signature provided by Google for this product
     */
    @NonNull
    String getInAppDataSignature();
}
