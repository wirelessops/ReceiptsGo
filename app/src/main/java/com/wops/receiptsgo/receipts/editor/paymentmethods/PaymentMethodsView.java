package com.wops.receiptsgo.receipts.editor.paymentmethods;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.util.List;

import com.wops.receiptsgo.model.PaymentMethod;
import io.reactivex.functions.Consumer;

/**
 * A view contract for managing the payment methods
 */
public interface PaymentMethodsView {

    /**
     * @return a {@link Consumer} that will toggle the payment method field visibility based on a
     * {@link Boolean} value, where {@code true} indicates that it's visible and {@code false}
     * indicates that it is not
     */
    @NonNull
    @UiThread
    Consumer<? super Boolean> togglePaymentMethodFieldVisibility();

    void displayPaymentMethods(List<PaymentMethod> list);

}
