package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;

public class ConsumablePurchase extends AbstractManagedProduct {

    public static final String GOOGLE_PRODUCT_TYPE = "inapp";

    public ConsumablePurchase(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseData,
                              @NonNull String purchaseToken, @NonNull String inAppDataSignature) {
        super(inAppPurchase, purchaseData, purchaseToken, inAppDataSignature);
    }

}
