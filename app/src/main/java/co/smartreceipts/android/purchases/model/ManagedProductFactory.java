package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

public class ManagedProductFactory {

    private final InAppPurchase inAppPurchase;
    private final String purchaseData;
    private final String inAppDataSignature;

    public ManagedProductFactory(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseData,
                                 @NonNull String inAppDataSignature) {
        this.inAppPurchase = Preconditions.checkNotNull(inAppPurchase);
        this.purchaseData = Preconditions.checkNotNull(purchaseData);
        this.inAppDataSignature = Preconditions.checkNotNull(inAppDataSignature);
    }

    @NonNull
    public ManagedProduct get() throws JSONException {
        final String purchaseToken;
        if (!TextUtils.isEmpty(purchaseData)) {
            final JSONObject purchaseDataJson = new JSONObject(purchaseData);
            purchaseToken = purchaseDataJson.getString("purchaseToken");
        }  else {
            purchaseToken = "";
        }
        if (Subscription.class.equals(inAppPurchase.getType())) {
            return new Subscription(inAppPurchase, purchaseData, purchaseToken, inAppDataSignature);
        } else if (ConsumablePurchase.class.equals(inAppPurchase.getType())) {
            return new ConsumablePurchase(inAppPurchase, purchaseData, purchaseToken, inAppDataSignature);
        } else {
            throw new IllegalArgumentException("Unsupported purchase type");
        }
    }
}
