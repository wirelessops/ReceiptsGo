package co.smartreceipts.android.currency.widget;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.utils.Supplier;
import co.smartreceipts.android.widget.mvp.BasePresenter;
import io.reactivex.Observable;
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
    private final Supplier<String> defaultCurrencyCodeSupplier;
    private final Bundle savedInstanceState;

    private String lastSelectedCurrencyCode;

    public CurrencyListEditorPresenter(@NonNull CurrencyListEditorView view,
                                       @NonNull DatabaseHelper databaseHelper,
                                       @NonNull Supplier<String> defaultCurrencyCodeSupplier,
                                       @Nullable Bundle savedInstanceState) {
        super(view);
        this.databaseHelper = Preconditions.checkNotNull(databaseHelper);
        this.defaultCurrencyCodeSupplier = Preconditions.checkNotNull(defaultCurrencyCodeSupplier);
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    @CallSuper
    public void subscribe() {
        // A ConnectableObservable resembles an ordinary Observable, but it does not begin emitting until #connect is called
        final ConnectableObservable<List<CharSequence>> currenciesConnectableObservable = Observable.fromCallable(this.databaseHelper::getCurrenciesList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();

        this.compositeDisposable.add(currenciesConnectableObservable
                .subscribe(view.displayCurrencies()));

        //noinspection Convert2MethodRef
        this.compositeDisposable.add(currenciesConnectableObservable
                .map(currenciesList -> {
                    final String currencyCode;
                    if (savedInstanceState != null && savedInstanceState.containsKey(OUT_STATE_SELECTED_CURRENCY_POSITION)) {
                        currencyCode = savedInstanceState.getString(OUT_STATE_SELECTED_CURRENCY_POSITION);
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


        this.compositeDisposable.add(currenciesConnectableObservable
                    .flatMap(currenciesList -> {
                        //noinspection ConstantConditions
                        return view.currencyClicks()
                                .filter(currencyIndex -> currencyIndex >= 0)
                                .map(currenciesList::get)
                                .map(CharSequence::toString);
                    })
                    .subscribe(currencyCode -> {
                        this.lastSelectedCurrencyCode = currencyCode;
                    }));


        // Call #connect to start out emissions
        this.compositeDisposable.add(currenciesConnectableObservable.connect());
    }

    public void onSaveInstanceState(@Nullable Bundle outState) {
        if (outState != null) {
            outState.putString(OUT_STATE_SELECTED_CURRENCY_POSITION, lastSelectedCurrencyCode);
        }
    }

}
