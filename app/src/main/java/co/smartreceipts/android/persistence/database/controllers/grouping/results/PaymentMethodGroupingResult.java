package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.PaymentMethod;

public class PaymentMethodGroupingResult {

    private final PaymentMethod paymentMethod;

    /**
     * Price in trip's currency
     */
    private final float price;

    public PaymentMethodGroupingResult(PaymentMethod paymentMethod, float price) {
        this.paymentMethod = paymentMethod;
        this.price = price;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public float getPrice() {
        return price;
    }
}
