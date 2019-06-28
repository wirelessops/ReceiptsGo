package co.smartreceipts.android.currency.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for interacting with a list of supported currencies
 */
public interface CurrencyListEditorView {

    /**
     * @return a {@link Consumer} that can display a list of currencies
     */
    @NonNull
    @UiThread
    Consumer<? super List<CharSequence>> displayCurrencies();

    /**
     * @return a {@link Consumer} that can display the actively selected currency
     */
    @NonNull
    @UiThread
    Consumer<? super Integer> displayCurrencySelection();

    /**
     * @return an {@link Observable} that will emit an {@link Integer}, which corresponds to the
     * position of the clicked currency as determined via the {@link #displayCurrencies()} list
     */
    @NonNull
    @UiThread
    Observable<Integer> currencyClicks();

}
