package co.smartreceipts.android.receipts.editor.pricing;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import io.reactivex.Observable;

/**
 * A view contract for managing receipt prices and taxes, which can be edited by the end user
 */
public interface EditableReceiptPricingView extends ReceiptPricingView {

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the receipt price
     */
    @NonNull
    @UiThread
    Observable<CharSequence> getReceiptPriceChanges();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the receipt tax
     */
    @NonNull
    @UiThread
    Observable<CharSequence> getReceiptTaxChanges();
}
