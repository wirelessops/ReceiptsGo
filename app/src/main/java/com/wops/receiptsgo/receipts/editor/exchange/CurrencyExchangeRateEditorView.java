package com.wops.receiptsgo.receipts.editor.exchange;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.hadisatrio.optional.Optional;

import org.joda.money.CurrencyUnit;

import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.gson.ExchangeRate;
import com.wops.receiptsgo.widget.model.UiIndicator;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for exchanging one currency to another
 */
public interface CurrencyExchangeRateEditorView {

    /**
     * @return a {@link Consumer} that will toggle the exchange rate field visibility based on a
     * {@link Boolean} value where {@code true} indicates that it's visible and {@code false} indicates
     * that it is not
     */
    @NonNull
    @UiThread
    Consumer<? super Boolean> toggleExchangeRateFieldVisibility();

    /**
     * @return a {@link Consumer} that will display the current {@link UiIndicator} for the {@link ExchangeRate}
     */
    @NonNull
    @UiThread
    Consumer<? super UiIndicator<ExchangeRate>> displayExchangeRate();

    /**
     * @return a {@link Consumer} that will display the base {@link CurrencyUnit} for a given {@link Trip}
     */
    @NonNull
    @UiThread
    Consumer<? super CurrencyUnit> displayBaseCurrency();

    /**
     * @return a {@link Consumer} that will display the exchanged {@link Price}, using the base currency
     * for a given {@link Trip}
     */
    @NonNull
    @UiThread
    Consumer<? super Optional<Price>> displayExchangedPriceInBaseCurrency();

    /**
     * @return the current {@link String} text of selection
     */
    @NonNull
    String getCurrencySelectionText();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the exchange rate
     */
    @NonNull
    @UiThread
    Observable<CharSequence> getExchangeRateChanges();

    /**
     * @return an {@link Observable} that will emit a {@link CharSequence} any time the user changes the value for
     * the exchanged {@link Price}, using the base currency for a given {@link Trip}
     */
    @NonNull
    @UiThread
    Observable<CharSequence> getExchangedPriceInBaseCurrencyChanges();

    /**
     * @return an {@link Observable} that will emit a {@link Boolean} any time the user changes the focus of
     * the view that holds the exchanged {@link Price}, using the base currency for a given {@link Trip}
     */
    @NonNull
    @UiThread
    Observable<Boolean> getExchangedPriceInBaseCurrencyFocusChanges();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever the user taps the retry button
     */
    @NonNull
    @UiThread
    Observable<Object> getUserInitiatedExchangeRateRetries();
}
