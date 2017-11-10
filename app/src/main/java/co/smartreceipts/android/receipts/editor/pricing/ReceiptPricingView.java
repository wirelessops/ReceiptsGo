package co.smartreceipts.android.receipts.editor.pricing;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import io.reactivex.functions.Consumer;

/**
 * A view contract for managing receipt prices and taxes
 */
public interface ReceiptPricingView {

    /**
     * @return a {@link Consumer} that will display the {@link Price} for the current {@link Receipt}
     */
    @NonNull
    @UiThread
    Consumer<? super Price> displayReceiptPrice();

    /**
     * @return a {@link Consumer} that will display the tax {@link Price} for the current {@link Receipt}
     */
    @NonNull
    @UiThread
    Consumer<? super Price> displayReceiptTax();

    /**
     * @return a {@link Consumer} that will toggle the tax field visibility based on a {@link Boolean} value
     * where {@code true} indicates that it's visible and {@code false} indicates that it is not
     */
    @NonNull
    @UiThread
    Consumer<? super Boolean> toggleReceiptTaxFieldVisibility();

}
