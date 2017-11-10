package co.smartreceipts.android.receipts.editor.pricing;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for managing receipt prices and taxes, which can be edited by the end user
 */
public interface EditableReceiptPricingView extends ReceiptPricingView {

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the receipt price
     */
    @NonNull
    Observable<CharSequence> getReceiptPriceChanges();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the receipt tax
     */
    @NonNull
    Observable<CharSequence> getReceiptTaxChanges();
}
