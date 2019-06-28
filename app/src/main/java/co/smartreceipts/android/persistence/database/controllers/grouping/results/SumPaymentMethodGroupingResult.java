package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;

public class SumPaymentMethodGroupingResult {

    private final PaymentMethod paymentMethod;

    private final Price price;

    public SumPaymentMethodGroupingResult(@NonNull PaymentMethod paymentMethod, @NonNull Price price) {
        this.paymentMethod = Preconditions.checkNotNull(paymentMethod);
        this.price = Preconditions.checkNotNull(price);
    }

    @NonNull
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @NonNull
    public Price getPrice() {
        return price;
    }
}
