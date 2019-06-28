package co.smartreceipts.android.purchases.model;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public abstract class AbstractManagedProduct implements ManagedProduct {

    private final InAppPurchase inAppPurchase;
    private final String purchaseData;
    private final String purchaseToken;
    private final String inAppDataSignature;

    public AbstractManagedProduct(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseData,
                                  @NonNull String purchaseToken, @NonNull String inAppDataSignature) {
        this.inAppPurchase = Preconditions.checkNotNull(inAppPurchase);
        this.purchaseData = Preconditions.checkNotNull(purchaseData);
        this.purchaseToken = Preconditions.checkNotNull(purchaseToken);
        this.inAppDataSignature = Preconditions.checkNotNull(inAppDataSignature);
    }

    @NonNull
    @Override
    public InAppPurchase getInAppPurchase() {
        return inAppPurchase;
    }

    @NonNull
    @Override
    public String getPurchaseData() {
        return purchaseData;
    }

    @NonNull
    @Override
    public String getPurchaseToken() {
        return purchaseToken;
    }

    @NonNull
    @Override
    public String getInAppDataSignature() {
        return inAppDataSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractManagedProduct)) return false;

        AbstractManagedProduct that = (AbstractManagedProduct) o;

        if (inAppPurchase != that.inAppPurchase) return false;
        if (!purchaseData.equals(that.purchaseData)) return false;
        if (!purchaseToken.equals(that.purchaseToken)) return false;
        return inAppDataSignature.equals(that.inAppDataSignature);

    }

    @Override
    public int hashCode() {
        int result = inAppPurchase.hashCode();
        result = 31 * result + purchaseData.hashCode();
        result = 31 * result + purchaseToken.hashCode();
        result = 31 * result + inAppDataSignature.hashCode();
        return result;
    }
}
