package co.smartreceipts.android.receipts.editor.exchange;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.math.BigDecimal;
import java.sql.Date;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.currency.widget.CurrencyListEditorView;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.receipts.editor.date.ReceiptDateView;
import co.smartreceipts.android.receipts.editor.pricing.EditableReceiptPricingView;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.utils.rx.PriceCharSequenceToBigDecimalObservableTransformer;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.mvp.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * A default presenter implementation to manage exchange rates for receipts
 */
public class CurrencyExchangeRateEditorPresenter extends BasePresenter<CurrencyExchangeRateEditorView> {

    private final EditableReceiptPricingView receiptPricingView;
    private final CurrencyListEditorView currencyListEditorView;
    private final ReceiptDateView receiptDateView;
    private final ExchangeRateServiceManager exchangeRateServiceManager;
    private final DatabaseHelper databaseHelper;
    private final Trip trip;
    private final Receipt editableReceipt;
    private final Bundle savedInstanceState;
    private final Scheduler ioScheduler;
    private final Scheduler computationScheduler;
    private final Scheduler mainThreadScheduler;

    private int skipCount = 0;

    public CurrencyExchangeRateEditorPresenter(@NonNull CurrencyExchangeRateEditorView view,
                                               @NonNull EditableReceiptPricingView receiptPricingView,
                                               @NonNull CurrencyListEditorView currencyListEditorView,
                                               @NonNull ReceiptDateView receiptDateView,
                                               @NonNull ExchangeRateServiceManager exchangeRateServiceManager,
                                               @NonNull DatabaseHelper databaseHelper,
                                               @NonNull Trip trip,
                                               @Nullable Receipt editableReceipt,
                                               @Nullable Bundle savedInstanceState) {
        this(view, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, editableReceipt, savedInstanceState, Schedulers.io(), Schedulers.computation(), AndroidSchedulers.mainThread());
    }

    public CurrencyExchangeRateEditorPresenter(@NonNull CurrencyExchangeRateEditorView view,
                                               @NonNull EditableReceiptPricingView receiptPricingView,
                                               @NonNull CurrencyListEditorView currencyListEditorView,
                                               @NonNull ReceiptDateView receiptDateView,
                                               @NonNull ExchangeRateServiceManager exchangeRateServiceManager,
                                               @NonNull DatabaseHelper databaseHelper,
                                               @NonNull Trip trip,
                                               @Nullable Receipt editableReceipt,
                                               @Nullable Bundle savedInstanceState,
                                               @NonNull Scheduler ioScheduler,
                                               @NonNull Scheduler computationScheduler,
                                               @NonNull Scheduler mainThreadScheduler) {
        super(view);
        this.receiptPricingView = Preconditions.checkNotNull(receiptPricingView);
        this.currencyListEditorView = Preconditions.checkNotNull(currencyListEditorView);
        this.receiptDateView = Preconditions.checkNotNull(receiptDateView);
        this.exchangeRateServiceManager = Preconditions.checkNotNull(exchangeRateServiceManager);
        this.databaseHelper = Preconditions.checkNotNull(databaseHelper);
        this.trip = Preconditions.checkNotNull(trip);
        this.editableReceipt = editableReceipt;
        this.savedInstanceState = savedInstanceState;
        this.ioScheduler = Preconditions.checkNotNull(ioScheduler);
        this.computationScheduler = Preconditions.checkNotNull(computationScheduler);
        this.mainThreadScheduler = Preconditions.checkNotNull(mainThreadScheduler);
    }

    @Override
    public void subscribe() {
        // Reset our previous skip count. We use this to handle situations in which we've cached a currency that != trip when savedState == null
        skipCount = 0;

        // This will emit a value whenever a currency is selected from our list
        final ConnectableObservable<String> selectedCurrencyConnectableObservable = Observable.fromCallable(this.databaseHelper::getCurrenciesList)
                .subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
                .flatMap(currenciesList -> {
                    //noinspection ConstantConditions
                    return currencyListEditorView.currencyClicks()
                            .filter(currencyIndex -> currencyIndex >= 0)
                            .map(currenciesList::get);
                })
                .map(CharSequence::toString)
                .publish();

        // This will emit a value when either the selected currency or selected date changes
        final ConnectableObservable<Pair<String, Date>> currencyDatePairConnectableObservable = Observable.combineLatest(selectedCurrencyConnectableObservable, receiptDateView.getReceiptDateChanges(), Pair::new)
                .publish();

        // edit == NULL => savedInstanceState != null && skipCount++ > 0
        // edit != NULL => saved == null && skipCount > 0
        // edit != NULL => saved != null && skipCount > 0

        // Fetch the exchange rate whenever the user changes the currency or date
        this.compositeDisposable.add(currencyDatePairConnectableObservable
                .skipWhile(currencyDatePair -> {
                    if (editableReceipt != null) {
                        // Note: Don't initiate the auto refresh if we're editing a receipt with the same currency
                        return editableReceipt.getPrice().getCurrencyCode().equals(currencyDatePair.first);
                    } else {
                        // Note: We want to skip the initial value unless it's our first time launching this
                        return savedInstanceState != null && skipCount++ > 0;
                    }
                })
                .switchMap(currencyDatePair -> {
                    if (!trip.getDefaultCurrencyCode().equals(currencyDatePair.first)) {
                        //noinspection ConstantConditions
                        return exchangeRateServiceManager.getExchangeRate(currencyDatePair.second, currencyDatePair.first, trip.getDefaultCurrencyCode());
                    } else {
                        // When the currency is the same as the trip currency, let's clear out the current value (ie set to empty)
                        return Observable.just(UiIndicator.<ExchangeRate>success());
                    }
                })
                .observeOn(mainThreadScheduler)
                .subscribe(view.displayExchangeRate()));

        // Fetch the exchange rate whenever the user clicks the "retry" button. Note: This variant can also attempt a purchase
        this.compositeDisposable.add(currencyDatePairConnectableObservable
                .switchMap(currencyDatePair -> view.getUserInitiatedExchangeRateRetries().map(userRetry -> currencyDatePair))
                .doOnNext(currencyDatePair -> Logger.info(CurrencyExchangeRateEditorPresenter.this, "User clicked to initiate exchange rate retry"))
                .flatMap(currencyDatePair -> {
                    //noinspection ConstantConditions
                    return exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(currencyDatePair.second, currencyDatePair.first, trip.getDefaultCurrencyCode());
                })
                .doOnError(exception -> Logger.error(CurrencyExchangeRateEditorPresenter.this, "Failed to initiate user retry", exception))
                .observeOn(mainThreadScheduler)
                .subscribe(view.displayExchangeRate()));

        // This will emit a char sequence whenever the user changes the exchange rate
        final ConnectableObservable<CharSequence> exchangeRateChangesConnectableObservable = view.getExchangeRateChanges()
                .publish();

        // This will emit a char sequence whenever the user changes the exchanged price in base currency
        final ConnectableObservable<CharSequence> exchangedPriceInBaseCurrencyChanges = view.getExchangedPriceInBaseCurrencyChanges()
                .publish();

        // This will emit a decimal value whenever the user changes the exchange rate
        final ConnectableObservable<Optional<BigDecimal>> exchangeRateChangesAsDecimalConnectableObservable = exchangeRateChangesConnectableObservable
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .publish();

        // This will emit a decimal value whenever the user changes the receipt price
        final ConnectableObservable<Optional<BigDecimal>> receiptPriceChangesAsDecimalConnectableObservable = receiptPricingView.getReceiptPriceChanges()
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .publish();

        // This will emit a decimal value whenever the user changes the exchanged price in base currency
        final ConnectableObservable<Optional<BigDecimal>> exchangedPriceInBaseCurrencyAsDecimalChanges = exchangedPriceInBaseCurrencyChanges
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .publish();

        // For the selected currency, multiple the exchange rate by receipt price (whenever either changes) to get the base currency total
        this.compositeDisposable.add(Observable.combineLatest(
                    receiptPriceChangesAsDecimalConnectableObservable,
                    exchangeRateChangesAsDecimalConnectableObservable,
                    selectedCurrencyConnectableObservable,
                    (priceDecimal, exchangeRateDecimal, selectedReceiptCurrencyCode) -> {
                        if (priceDecimal.isPresent() && exchangeRateDecimal.isPresent()) {
                            final PriceBuilderFactory priceBuilderFactory = new PriceBuilderFactory();
                            priceBuilderFactory.setCurrency(trip.getDefaultCurrencyCode());
                            priceBuilderFactory.setPrice(priceDecimal.get().multiply(exchangeRateDecimal.get()));
                            return Optional.of(priceBuilderFactory.build());
                        } else {
                            return Optional.<Price>absent();
                        }
                    }
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(exchangedPriceInBaseCurrency -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Updating the exchanged price in base currency: {}", exchangedPriceInBaseCurrency))
                .flatMapSingle(exchangedPriceInBaseCurrency ->
                        // Note: we use this to ensure we don't mess things up while the user is typing
                        view.getExchangedPriceInBaseCurrencyFocusChanges()
                        .firstOrError()
                        .map(isFocused -> {
                            if (!isFocused) {
                                return Optional.of(exchangedPriceInBaseCurrency);
                            } else {
                                return Optional.<Price>absent();
                            }
                        })
                        .doOnSuccess(ignored -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Allowing base rate changes as the field is not currently focuses"))
                )
                .filter(Optional::isPresent)
                .doOnError(exception -> Logger.error(CurrencyExchangeRateEditorPresenter.this, "Failed to exchanged price in base currency", exception))
                .subscribeOn(computationScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe(view.displayExchangedPriceInBaseCurrency()));

        // For the selected currency, divide the base currency total by the exchange rate (whenever either changes) to get the receipt price
        this.compositeDisposable.add(exchangedPriceInBaseCurrencyAsDecimalChanges
                .withLatestFrom(receiptPriceChangesAsDecimalConnectableObservable, selectedCurrencyConnectableObservable, (exchangedPriceInBaseCurrency, receiptPrice, selectedReceiptCurrencyCode) -> {
                    if (exchangedPriceInBaseCurrency.isPresent() && receiptPrice.isPresent()) {
                        if (receiptPrice.get().compareTo(BigDecimal.ZERO) == 0) {
                            return Optional.<ExchangeRate>absent();
                        } else {
                            final ExchangeRateBuilderFactory factory = new ExchangeRateBuilderFactory();
                            final BigDecimal rate = exchangedPriceInBaseCurrency.get().divide(receiptPrice.get(), ExchangeRate.PRECISION, BigDecimal.ROUND_HALF_UP);
                            factory.setBaseCurrency(selectedReceiptCurrencyCode);
                            factory.setRate(trip.getDefaultCurrencyCode(), rate); // ie from receipt currency to trip currency
                            return Optional.of(factory.build());
                        }
                    } else {
                        return Optional.<ExchangeRate>absent();
                    }
                })
                .doOnNext(exchangeRateOptional -> {
                    if (!exchangeRateOptional.isPresent()) {
                        Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Ignoring divide by zero condition...");
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(exchangeRate -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Updating exchange rate from user input: {}", exchangeRate))
                .map(UiIndicator::success)
                .doOnError(exception -> Logger.error(CurrencyExchangeRateEditorPresenter.this, "Failed to exchanged rate update from base", exception))
                .subscribeOn(computationScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe(view.displayExchangeRate()));

        // If the user ever clears the exchange rate, we should also clear the exchanged price
        this.compositeDisposable.add(exchangeRateChangesConnectableObservable
                .filter(exchangeRateText -> exchangeRateText.length() == 0)
                .map(ignored -> Optional.<Price>absent())
                .doOnNext(ignored -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Clearing exchanged price now that exchange rate is empty"))
                .subscribe(view.displayExchangedPriceInBaseCurrency()));

        // If the user ever clears the exchanged price, we should also clear the exchanged rate
        this.compositeDisposable.add(exchangedPriceInBaseCurrencyChanges
                .skip(1) // We ignore the initial value setter
                .filter(exchangedPriceInBaseCurrencyText -> exchangedPriceInBaseCurrencyText.length() == 0)
                .map(ignored -> Optional.<Price>absent())
                .doOnNext(ignored -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Clearing exchange rate now that exchanged price is empty"))
                .map(exchangedPriceInBaseCurrencyText -> UiIndicator.<ExchangeRate>success())
                .subscribe(view.displayExchangeRate()));

        // Toggle the exchange rate field any time the user changes it
        compositeDisposable.add(selectedCurrencyConnectableObservable
                .map(selectedCurrencyCode -> !trip.getDefaultCurrencyCode().equals(selectedCurrencyCode))
                .doOnNext(exchangeFieldsAreVisible -> Logger.debug(CurrencyExchangeRateEditorPresenter.this, "Exchange rate field visibility -> {}", exchangeFieldsAreVisible))
                .subscribe(view.toggleExchangeRateFieldVisibility()));

        // Display the base currency of the price
        compositeDisposable.add(Observable.just(trip)
                .map(Trip::getDefaultCurrencyCode)
                .map(PriceCurrency::getInstance)
                .subscribe(view.displayBaseCurrency()));

        // And start our ConnectableObservables
        compositeDisposable.add(exchangeRateChangesConnectableObservable.connect());
        compositeDisposable.add(exchangedPriceInBaseCurrencyChanges.connect());
        compositeDisposable.add(currencyDatePairConnectableObservable.connect());
        compositeDisposable.add(selectedCurrencyConnectableObservable.connect());
        compositeDisposable.add(exchangeRateChangesAsDecimalConnectableObservable.connect());
        compositeDisposable.add(receiptPriceChangesAsDecimalConnectableObservable.connect());
        compositeDisposable.add(exchangedPriceInBaseCurrencyAsDecimalChanges.connect());

        // Display the exchange rate for the receipt that we're currently editing
        compositeDisposable.add(Observable.just(Optional.ofNullable(editableReceipt))
                .filter(Optional::isPresent)
                .filter(ignored -> savedInstanceState == null)
                .map(Optional::get)
                .map(receipt -> receipt.getPrice().getExchangeRate())
                .filter(exchangeRate -> exchangeRate.supportsExchangeRateFor(trip.getDefaultCurrencyCode()))
                .map(UiIndicator::success)
                .subscribe(view.displayExchangeRate()));

        // Since we may navigate back to this screen, let's add a simple check to hide the exchange rates if the currency = trip currency
        compositeDisposable.add(Observable.just(view.getCurrencySelectionText())
                .map(editorCurrency -> !trip.getDefaultCurrencyCode().equals(editorCurrency))
                .subscribe(view.toggleExchangeRateFieldVisibility()));
    }
}
