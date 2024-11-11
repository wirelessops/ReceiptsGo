package com.wops.receiptsgo.persistence.database.controllers.alterations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import io.reactivex.Single;

public class PaymentMethodsTableActionAlterations extends StubTableActionAlterations<PaymentMethod> {

    private final ReceiptsTable receiptsTable;

    public PaymentMethodsTableActionAlterations(@NonNull ReceiptsTable receiptsTable) {
        this.receiptsTable = Preconditions.checkNotNull(receiptsTable);
    }

    @NonNull
    @Override
    public Single<PaymentMethod> postUpdate(@NonNull PaymentMethod oldPaymentMethod, @Nullable PaymentMethod newPaymentMethod) {
        return super.postUpdate(oldPaymentMethod, newPaymentMethod)
                .doOnSuccess(ignored -> receiptsTable.clearCache());
    }

    @NonNull
    @Override
    public Single<PaymentMethod> postDelete(@Nullable PaymentMethod paymentMethod) {
        return super.postDelete(paymentMethod)
                .doOnSuccess(ignored -> receiptsTable.clearCache());
    }
}
