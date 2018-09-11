package co.smartreceipts.android.purchases.apis.purchases;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.purchases.model.ManagedProduct;

public class PurchaseRequest {

    private String signature;
    private String receipt;
    private String pay_service;
    private String goal;

    public PurchaseRequest(@NonNull ManagedProduct managedProduct, @NonNull String goal) {
        this.signature = Preconditions.checkNotNull(managedProduct.getInAppDataSignature());
        this.goal = Preconditions.checkNotNull(goal);
        this.receipt = Preconditions.checkNotNull(managedProduct.getPurchaseData());
        this.pay_service = "Google Play";
    }

}
