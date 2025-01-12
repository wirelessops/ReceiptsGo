package com.wops.receiptsgo.currency.widget;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding3.widget.RxAdapterView;

import java.util.List;

import com.wops.receiptsgo.utils.Supplier;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Provides a default implementation of the {@link CurrencyListEditorView}, since we tend to use the
 * same list styles for all of our classes
 */
public class DefaultCurrencyListEditorView implements CurrencyListEditorView {

    private final Context context;
    private final Supplier<Spinner> currencySpinnerSupplier;

    // Note: We cache this, since Android doesn't support the registration of multiple OnItemSelectedListener instances
    private Observable<Integer> currencyClicks;
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
        return integer -> currencySpinnerSupplier.get().setSelection(integer);
    }

    @Override
    @NonNull
    @UiThread
    public synchronized Observable<Integer> currencyClicks() {
        if (currencyClicks == null) {
            currencyClicks = RxAdapterView.itemSelections(currencySpinnerSupplier.get()).share();
        }
        return currencyClicks;
    }
}
