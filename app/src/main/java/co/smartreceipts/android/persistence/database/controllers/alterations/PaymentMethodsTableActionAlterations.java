package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
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
