package com.wops.receiptsgo.receipts.editor.exchange;

import android.os.Bundle;

import com.hadisatrio.optional.Optional;

import org.joda.money.CurrencyUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.wops.receiptsgo.currency.widget.CurrencyListEditorView;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.ExchangeRateBuilderFactory;
import com.wops.receiptsgo.model.factory.PriceBuilderFactory;
import com.wops.receiptsgo.model.gson.ExchangeRate;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.receipts.editor.date.ReceiptDateView;
import com.wops.receiptsgo.receipts.editor.pricing.EditableReceiptPricingView;
import com.wops.receiptsgo.utils.TestLocaleToggler;
import com.wops.receiptsgo.widget.model.UiIndicator;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CurrencyExchangeRateEditorPresenterTest {

    private static final List<CharSequence> CURRENCIES = Arrays.asList("USD", "EUR", "GBP");
    private static final String TRIP_CURRENCY = "USD";
    private static final String RECEIPT_CURRENCY = "EUR";
    private static final ExchangeRate EXCHANGE_RATE = new ExchangeRateBuilderFactory().setBaseCurrency(RECEIPT_CURRENCY).setRate(TRIP_CURRENCY, new BigDecimal("0.5")).build();

    CurrencyExchangeRateEditorPresenter presenter;

    @Mock
    CurrencyExchangeRateEditorView currencyExchangeRateEditorView;

    @Mock
    EditableReceiptPricingView receiptPricingView;

    @Mock
    CurrencyListEditorView currencyListEditorView;

    @Mock
    ReceiptDateView receiptDateView;

    @Mock
    ExchangeRateServiceManager exchangeRateServiceManager;

    @Mock
    DatabaseHelper databaseHelper;

    @Mock
    Trip trip;

    @Mock
    Receipt editableReceipt;

    @Mock
    Price price;

    Bundle savedInstanceState = new Bundle();

    @Mock
    Consumer<Boolean> toggleExchangeRateFieldVisibilityConsumer;

    @Mock
    Consumer<UiIndicator<ExchangeRate>> displayExchangeRateConsumer;

    @Mock
    Consumer<CurrencyUnit> displayBaseCurrencyConsumer;

    @Mock
    Consumer<Optional<Price>> displayExchangedPriceInBaseCurrencyConsumer;

    BehaviorSubject<CharSequence> exchangeRateChanges = BehaviorSubject.create();

    BehaviorSubject<CharSequence> exchangedPriceInBaseCurrencyChanges = BehaviorSubject.create();

    BehaviorSubject<Boolean> exchangedPriceInBaseCurrencyFocusChanges = BehaviorSubject.create();

    PublishSubject<Object> userInitiatedExchangeRateRetries = PublishSubject.create();

    BehaviorSubject<CharSequence> receiptPriceChanges = BehaviorSubject.create();

    BehaviorSubject<Integer> currencyClicks = BehaviorSubject.create();

    BehaviorSubject<Date> receiptDateChanges = BehaviorSubject.create();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestLocaleToggler.setDefaultLocale(Locale.US);
        when(trip.getDefaultCurrencyCode()).thenReturn(TRIP_CURRENCY);
        doReturn(toggleExchangeRateFieldVisibilityConsumer).when(currencyExchangeRateEditorView).toggleExchangeRateFieldVisibility();
        doReturn(displayExchangeRateConsumer).when(currencyExchangeRateEditorView).displayExchangeRate();
        doReturn(displayBaseCurrencyConsumer).when(currencyExchangeRateEditorView).displayBaseCurrency();
        doReturn(displayExchangedPriceInBaseCurrencyConsumer).when(currencyExchangeRateEditorView).displayExchangedPriceInBaseCurrency();
        when(currencyExchangeRateEditorView.getExchangeRateChanges()).thenReturn(exchangeRateChanges);
        when(currencyExchangeRateEditorView.getExchangedPriceInBaseCurrencyChanges()).thenReturn(exchangedPriceInBaseCurrencyChanges);
        when(currencyExchangeRateEditorView.getExchangedPriceInBaseCurrencyFocusChanges()).thenReturn(exchangedPriceInBaseCurrencyFocusChanges);
        when(currencyExchangeRateEditorView.getUserInitiatedExchangeRateRetries()).thenReturn(userInitiatedExchangeRateRetries);
        when(currencyExchangeRateEditorView.getCurrencySelectionText()).thenReturn(TRIP_CURRENCY);
        when(receiptPricingView.getReceiptPriceChanges()).thenReturn(receiptPriceChanges);
        when(currencyListEditorView.currencyClicks()).thenReturn(currencyClicks);
        when(receiptDateView.getReceiptDateChanges()).thenReturn(receiptDateChanges);
        when(databaseHelper.getCurrenciesList()).thenReturn(CURRENCIES);
        when(editableReceipt.getPrice()).thenReturn(price);
        when(price.getExchangeRate()).thenReturn(EXCHANGE_RATE);
        when(exchangeRateServiceManager.getExchangeRate(any(Date.class), anyString(), anyString())).thenReturn(Observable.just(UiIndicator.success(EXCHANGE_RATE)));
        when(exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(any(Date.class), anyString(), anyString())).thenReturn(Observable.just(UiIndicator.success(EXCHANGE_RATE)));
    }

    @After
    public void tearDown() {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void subscribeDisplaysBaseCurrency() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        verify(displayBaseCurrencyConsumer).accept(CurrencyUnit.of(TRIP_CURRENCY));
    }

    @Test
    public void subscribeDoesNotDisplayExchangeRateForNullReceipt() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        verify(displayExchangeRateConsumer, never()).accept(UiIndicator.success(EXCHANGE_RATE));
    }

    @Test
    public void subscribeDoesNotDisplayExchangeRateNonNullReceiptWithSavedState() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, editableReceipt, savedInstanceState, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        verify(displayExchangeRateConsumer, never()).accept(UiIndicator.success(EXCHANGE_RATE));
    }

    @Test
    public void subscribeDisplayExchangeRateNonNullReceiptWithNullSavedState() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, editableReceipt, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        verify(displayExchangeRateConsumer).accept(UiIndicator.success(EXCHANGE_RATE));
    }

    @Test
    public void subscribeAlwaysTogglesExchangeRateVisibility() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, editableReceipt, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        verify(toggleExchangeRateFieldVisibilityConsumer).accept(false);
    }

    @Test
    public void subscribeTogglesExchangeRateVisibilityAndListensToChangeEvents() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, editableReceipt, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        final InOrder inOrderVerifier = inOrder(toggleExchangeRateFieldVisibilityConsumer);
        inOrderVerifier.verify(toggleExchangeRateFieldVisibilityConsumer).accept(false);
        currencyClicks.onNext(0);
        inOrderVerifier.verify(toggleExchangeRateFieldVisibilityConsumer).accept(false);
        currencyClicks.onNext(1);
        inOrderVerifier.verify(toggleExchangeRateFieldVisibilityConsumer).accept(true);
    }

    @Test
    public void changingCurrencyToTripCurrencyClearsTheExchangeRate() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(0); // Call twice, since the real code uses an initial value observable
        receiptDateChanges.onNext(new Date(100L));
        verify(displayExchangeRateConsumer).accept(UiIndicator.success());
        verify(exchangeRateServiceManager, never()).getExchangeRate(any(Date.class), anyString(), anyString());
    }

    @Test
    public void changingCurrencyGetsExchangeRate() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(1); // Call twice, since the real code uses an initial value observable
        receiptDateChanges.onNext(new Date(100L));
        verify(displayExchangeRateConsumer).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager).getExchangeRate(any(Date.class), anyString(), anyString());
    }

    @Test
    public void changingDateGetsExchangeRate() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(1);
        receiptDateChanges.onNext(new Date(100L));
        receiptDateChanges.onNext(new Date(200L)); // Call twice, since the real code uses an initial value observable
        verify(displayExchangeRateConsumer, times(2)).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager, times(2)).getExchangeRate(any(Date.class), anyString(), anyString());
    }

    @Test
    public void changingDateFirstOnlyProducesOneExchangeRateChange() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(0);
        receiptDateChanges.onNext(new Date(100L));
        receiptDateChanges.onNext(new Date(200L)); // Call twice, since the real code uses an initial value observable
        currencyClicks.onNext(1);
        verify(displayExchangeRateConsumer).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager).getExchangeRate(any(Date.class), anyString(), anyString());
    }

    @Test
    public void onlyOneExchangeRateIsFetchedPerDay() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(0);
        receiptDateChanges.onNext(new Date(100L));
        receiptDateChanges.onNext(new Date(200L)); // Call twice, since the real code uses an initial value observable
        currencyClicks.onNext(1);
        currencyClicks.onNext(2);
        verify(displayExchangeRateConsumer, times(2)).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager, times(2)).getExchangeRate(any(Date.class), anyString(), anyString());
    }

    @Test
    public void userMultipleRetriesGetsExchangeRateOrPurchase() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(1);
        receiptDateChanges.onNext(new Date(200L));
        currencyClicks.onNext(2);
        userInitiatedExchangeRateRetries.onNext(new Object());
        userInitiatedExchangeRateRetries.onNext(new Object());
        userInitiatedExchangeRateRetries.onNext(new Object());
        verify(displayExchangeRateConsumer, atLeastOnce()).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager, times(3)).getExchangeRateOrInitiatePurchase(any(Date.class), anyString(), anyString());
    }

    @Test
    public void userRetryGetsExchangeRateOrPurchase() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();
        currencyClicks.onNext(1);
        receiptDateChanges.onNext(new Date(200L));
        currencyClicks.onNext(2);
        userInitiatedExchangeRateRetries.onNext(new Object());
        currencyClicks.onNext(1);
        receiptDateChanges.onNext(new Date(100L));
        verify(displayExchangeRateConsumer, atLeastOnce()).accept(UiIndicator.success(EXCHANGE_RATE));
        verify(exchangeRateServiceManager).getExchangeRateOrInitiatePurchase(any(Date.class), anyString(), anyString());
    }

    @Test
    public void userEditsPriceThenExchangeRateFields() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        exchangedPriceInBaseCurrencyFocusChanges.onNext(false);
        currencyClicks.onNext(1);

        receiptPriceChanges.onNext("1");
        receiptPriceChanges.onNext("10");
        receiptPriceChanges.onNext("10.");
        receiptPriceChanges.onNext("10.0");
        receiptPriceChanges.onNext("10.00");

        final InOrder inOrderVerifier = inOrder(displayExchangedPriceInBaseCurrencyConsumer);

        exchangeRateChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());

        exchangeRateChanges.onNext("0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0.");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0.1");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("1").build()));

        exchangeRateChanges.onNext("0.");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0.2");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        exchangeRateChanges.onNext("0.");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());
    }

    @Test
    public void userEditsPriceThenExchangeRateFieldsWithCommaForDecimal() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.FRANCE);

        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        exchangedPriceInBaseCurrencyFocusChanges.onNext(false);
        currencyClicks.onNext(1);

        receiptPriceChanges.onNext("1");
        receiptPriceChanges.onNext("10");
        receiptPriceChanges.onNext("10,");
        receiptPriceChanges.onNext("10,0");
        receiptPriceChanges.onNext("10,00");

        final InOrder inOrderVerifier = inOrder(displayExchangedPriceInBaseCurrencyConsumer);

        exchangeRateChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());

        exchangeRateChanges.onNext("0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0,");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0,1");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("1").build()));

        exchangeRateChanges.onNext("0,");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0,2");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        exchangeRateChanges.onNext("0,");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0").build()));

        exchangeRateChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());
    }

    @Test
    public void userEditsExchangeRateThenPriceFields() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        exchangedPriceInBaseCurrencyFocusChanges.onNext(false);
        currencyClicks.onNext(1);

        exchangeRateChanges.onNext("");
        exchangeRateChanges.onNext("0");
        exchangeRateChanges.onNext("0.1");
        exchangeRateChanges.onNext("0.");
        exchangeRateChanges.onNext("0.2");

        final InOrder inOrderVerifier = inOrder(displayExchangedPriceInBaseCurrencyConsumer);

        receiptPriceChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());

        receiptPriceChanges.onNext("1");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0.2").build()));

        receiptPriceChanges.onNext("10");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10.");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10.0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10.00");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10.0");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10.");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("10");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("2").build()));

        receiptPriceChanges.onNext("1");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.of(new PriceBuilderFactory().setCurrency(TRIP_CURRENCY).setPrice("0.2").build()));
    }

    @Test
    public void userEditsExchangeRateThenPriceFieldThatCausesDivisionByZero() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        exchangedPriceInBaseCurrencyFocusChanges.onNext(false);
        currencyClicks.onNext(1);

        exchangeRateChanges.onNext("");
        exchangeRateChanges.onNext("0");
        exchangeRateChanges.onNext("0.1");
        exchangeRateChanges.onNext("0.");
        exchangeRateChanges.onNext("0.2");

        final InOrder inOrderVerifier = inOrder(displayExchangedPriceInBaseCurrencyConsumer);

        receiptPriceChanges.onNext("");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer).accept(Optional.absent());

        receiptPriceChanges.onNext("0");
        receiptPriceChanges.onNext("0.0");
        receiptPriceChanges.onNext("0.00");
        receiptPriceChanges.onNext("0.000");
        inOrderVerifier.verify(displayExchangedPriceInBaseCurrencyConsumer, never()).accept(Optional.absent());
    }

    @Test
    public void userEditsPriceThenExchangedTotalInBaseCurrency() throws Exception {
        presenter = new CurrencyExchangeRateEditorPresenter(currencyExchangeRateEditorView, receiptPricingView, currencyListEditorView, receiptDateView, exchangeRateServiceManager, databaseHelper, trip, null, null, Schedulers.trampoline(), Schedulers.trampoline(), Schedulers.trampoline());
        presenter.subscribe();

        exchangedPriceInBaseCurrencyFocusChanges.onNext(true);
        currencyClicks.onNext(1);

        receiptPriceChanges.onNext("1");
        receiptPriceChanges.onNext("10");
        receiptPriceChanges.onNext("10.");
        receiptPriceChanges.onNext("10.0");
        receiptPriceChanges.onNext("10.00");

        final InOrder inOrderVerifier = inOrder(displayExchangeRateConsumer);

        exchangedPriceInBaseCurrencyChanges.onNext("");
        exchangedPriceInBaseCurrencyChanges.onNext("2");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2.");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2.0");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2.00");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2.0");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2.");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("2");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success(new ExchangeRateBuilderFactory().setRate(TRIP_CURRENCY, new BigDecimal("0.200000")).setBaseCurrency(RECEIPT_CURRENCY).build()));

        exchangedPriceInBaseCurrencyChanges.onNext("");
        inOrderVerifier.verify(displayExchangeRateConsumer).accept(UiIndicator.success());
    }

}
