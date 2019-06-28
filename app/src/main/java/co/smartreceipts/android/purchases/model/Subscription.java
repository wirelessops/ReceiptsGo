package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;

public class Subscription extends AbstractManagedProduct {

    public static final String GOOGLE_PRODUCT_TYPE = "subs";

    public Subscription(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseData,
                        @NonNull String purchaseToken, @NonNull String inAppDataSignature) {
        super(inAppPurchase, purchaseData, purchaseToken, inAppDataSignature);
    }
}
