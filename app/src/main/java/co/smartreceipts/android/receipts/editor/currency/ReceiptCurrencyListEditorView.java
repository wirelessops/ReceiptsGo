package co.smartreceipts.android.receipts.editor.currency;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for interacting with a list of supported currencies
 */
public interface ReceiptCurrencyListEditorView {

    @NonNull
    Consumer<? super List<CharSequence>> displayCurrencies();

    @NonNull
    Consumer<? super Integer> displayCurrencySelection();

    @Nullable
    Observable<Integer> currencyClicks();

}
