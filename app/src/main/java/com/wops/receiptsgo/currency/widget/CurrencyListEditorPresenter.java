package com.wops.receiptsgo.currency.widget;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.util.List;

import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.widget.mvp.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * A default presenter implementation to manage the fetching and displaying of a list of available
 * currencies for the end user to select.
 */
public class CurrencyListEditorPresenter extends BasePresenter<CurrencyListEditorView> {

    private static final String OUT_STATE_SELECTED_CURRENCY_POSITION = "out_state_selected_currency_position";

    private final DatabaseHelper databaseHelper;
    private final CurrencyCodeSupplier defaultCurrencyCodeSupplier;
    private final Bundle savedInstanceState;
    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    private int lastSelectedCurrencyCodeIndex = -1;

    public CurrencyListEditorPresenter(@NonNull CurrencyListEditorView view,
                                       @NonNull DatabaseHelper databaseHelper,
                                       @NonNull CurrencyCodeSupplier defaultCurrencyCodeSupplier,
                                       @Nullable Bundle savedInstanceState) {
        this(view, databaseHelper, defaultCurrencyCodeSupplier, savedInstanceState, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    CurrencyListEditorPresenter(@NonNull CurrencyListEditorView view,
                                @NonNull DatabaseHelper databaseHelper,
                                @NonNull CurrencyCodeSupplier defaultCurrencyCodeSupplier,
                                @Nullable Bundle savedInstanceState,
                                @NonNull Scheduler subscribeOnScheduler,
                                @NonNull Scheduler observeOnScheduler) {
        super(view);
        this.databaseHelper = Preconditions.checkNotNull(databaseHelper);
        this.defaultCurrencyCodeSupplier = Preconditions.checkNotNull(defaultCurrencyCodeSupplier);
        this.savedInstanceState = savedInstanceState;
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    @Override
    @CallSuper
    public void subscribe() {
        // A ConnectableObservable resembles an ordinary Observable, but it does not begin emitting until #connect is called
        final ConnectableObservable<List<CharSequence>> currenciesConnectableObservable = Observable.fromCallable(this.databaseHelper::getCurrenciesList)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .publish();

        // Display the full list of currencies
        this.compositeDisposable.add(currenciesConnectableObservable
                .subscribe(view.displayCurrencies()));

        // Ensure we always restore the "last" currency
        //noinspection Convert2MethodRef
        this.compositeDisposable.add(currenciesConnectableObservable
                .map(currenciesList -> {
                    final String currencyCode;
                    if (savedInstanceState != null && savedInstanceState.containsKey(OUT_STATE_SELECTED_CURRENCY_POSITION)) {
                        currencyCode = currenciesList.get(savedInstanceState.getInt(OUT_STATE_SELECTED_CURRENCY_POSITION)).toString();
                    } else if (lastSelectedCurrencyCodeIndex >= 0) {
                        currencyCode = currenciesList.get(lastSelectedCurrencyCodeIndex).toString();
                    } else {
                        currencyCode = defaultCurrencyCodeSupplier.get();
                    }

                    final int currencyPosition = currenciesList.indexOf(currencyCode);
                    if (currencyPosition >= 0) {
                        return currencyPosition;
                    } else {
                        return 0;
                    }
                })
                .subscribe(view.displayCurrencySelection()));

        // Handle selections
        this.compositeDisposable.add(currenciesConnectableObservable
                    .flatMap(currenciesList -> {
                        //noinspection ConstantConditions
                        return view.currencyClicks()
                                .filter(currencyIndex -> currencyIndex >= 0);
                    })
                    .doOnNext(currencyIndex -> lastSelectedCurrencyCodeIndex = currencyIndex)
                    .subscribe(view.displayCurrencySelection()));

        // Call #connect to start out emissions
        this.compositeDisposable.add(currenciesConnectableObservable.connect());
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (lastSelectedCurrencyCodeIndex >= 0) {
            outState.putInt(OUT_STATE_SELECTED_CURRENCY_POSITION, lastSelectedCurrencyCodeIndex);
        }
    }

}
