package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;

public class SumPaymentMethodGroupingResult {

    private final PaymentMethod paymentMethod;

    private final Price price;

    public SumPaymentMethodGroupingResult(PaymentMethod paymentMethod, Price price) {
        this.paymentMethod = paymentMethod;
        this.price = price;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Price getPrice() {
        return price;
    }
}
