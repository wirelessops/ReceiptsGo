package co.smartreceipts.android.receipts.editor.exchange;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for exchanging one currency to another
 */
public interface CurrencyExchangeRateEditorView {

    /**
     * @return a {@link Consumer} that will display the base {@link PriceCurrency} for a given {@link Trip}
     */
    @NonNull
    @UiThread
    Consumer<? super PriceCurrency> displayBaseCurrency();

    /**
     * @return a {@link Consumer} that will display the current {@link ExchangeRate}
     */
    @NonNull
    @UiThread
    Consumer<? super ExchangeRate> displayExchangeRate();

    /**
     * @return a {@link Consumer} that will display the {@link Price} total in the base currency (after having been exchanged)
     */
    @NonNull
    @UiThread
    Consumer<? super Price> displayBaseCurrencyPrice();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the exchange rate
     */
    @NonNull
    Observable<CharSequence> getExchangeRateChanges();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the base currency price
     */
    @NonNull
    Observable<CharSequence> getBaseCurrencyPriceChanges();
}
