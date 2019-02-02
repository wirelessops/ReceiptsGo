package co.smartreceipts.android.receipts.editor.paymentmethods;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
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

}
