package com.wops.receiptsgo.receipts.editor.pricing;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Receipt;
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
     * @return a {@link Consumer} that will display the tax1 {@link Price} for the current {@link Receipt}
     */
    @NonNull
    @UiThread
    Consumer<? super Price> displayReceiptTax();

    /**
     * @return a {@link Consumer} that will display the tax2 {@link Price} for the current {@link Receipt}
     */
    @NonNull
    @UiThread
    Consumer<? super Price> displayReceiptTax2();

    /**
     * @return a {@link Consumer} that will toggle the tax1 field visibility based on a {@link Boolean} value
     * where {@code true} indicates that it's visible and {@code false} indicates that it is not
     */
    @NonNull
    @UiThread
    Consumer<? super Boolean> toggleReceiptTaxFieldVisibility();

    /**
     * @return a {@link Consumer} that will toggle the tax2 field visibility based on a {@link Boolean} value
     * where {@code true} indicates that it's visible and {@code false} indicates that it is not
     */
    @NonNull
    @UiThread
    Consumer<? super Boolean> toggleReceiptTax2FieldVisibility();

}
