package co.smartreceipts.android.currency.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding2.widget.RxAdapterView;

import java.util.List;

import co.smartreceipts.android.utils.Supplier;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Provides a default implementation of the {@link CurrencyListEditorView}, since we tend to use the
 * same list styles for all of our classes
 */
public class DefaultCurrencyListEditorView implements CurrencyListEditorView {

    private final Context context;
    private final Supplier<Spinner> currencySpinnerSupplier;

    /**
     * Default constructor for this class
     *
     * @param context the current {@link Context}
     * @param currencySpinnerSupplier a {@link Supplier} for the currency spinner. This must return a non null value
     *                               by the time the corresponding presenter is subscribed
     */
    public DefaultCurrencyListEditorView(@NonNull Context context, @NonNull Supplier<Spinner> currencySpinnerSupplier) {
        this.context = Preconditions.checkNotNull(context);
        this.currencySpinnerSupplier = Preconditions.checkNotNull(currencySpinnerSupplier);
    }

    @Override
    @NonNull
    @UiThread
    public Consumer<? super List<CharSequence>> displayCurrencies() {
        return (Consumer<List<CharSequence>>) currencies -> {
            final ArrayAdapter<CharSequence> currenciesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
            currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinnerSupplier.get().setAdapter(currenciesAdapter);
        };
    }

    @Override
    @NonNull
    @UiThread
    public Consumer<? super Integer> displayCurrencySelection() {
        return RxAdapterView.selection(currencySpinnerSupplier.get());
    }

    @Nullable
    @Override
    @UiThread
    public Observable<Integer> currencyClicks() {
        return RxAdapterView.itemSelections(currencySpinnerSupplier.get());
    }
}
