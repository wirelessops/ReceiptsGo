package co.smartreceipts.android.currency.widget;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import co.smartreceipts.android.persistence.DatabaseHelper;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CurrencyListEditorPresenterTest {

    private static final List<CharSequence> CURRENCIES = Arrays.asList("USD", "EUR");

    // Class under test
    CurrencyListEditorPresenter nullSavedStatePresenter;

    // Class under test
    CurrencyListEditorPresenter savedStatePresenter;

    @Mock
    CurrencyListEditorView view;

    @Mock
    DatabaseHelper database;

    @Mock
    CurrencyCodeSupplier currencyCodeSupplier;

    @Mock
    Consumer<List<CharSequence>> displayCurrenciesConsumer;

    @Mock
    Consumer<Integer> displayCurrencySelectionConsumer;

    PublishSubject<Integer> currencyClicks = PublishSubject.create();

    Bundle savedInstanceState = new Bundle();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(database.getCurrenciesList()).thenReturn(CURRENCIES);
        doReturn(displayCurrenciesConsumer).when(view).displayCurrencies();
        doReturn(displayCurrencySelectionConsumer).when(view).displayCurrencySelection();
        when(view.currencyClicks()).thenReturn(currencyClicks);
        nullSavedStatePresenter = new CurrencyListEditorPresenter(view, database, currencyCodeSupplier, null, Schedulers.trampoline(), Schedulers.trampoline());
        savedStatePresenter = new CurrencyListEditorPresenter(view, database, currencyCodeSupplier, savedInstanceState, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void subscribeDisplaysCurrenciesListForNullState() throws Exception {
        nullSavedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer).accept(CURRENCIES);
    }

    @Test
    public void subscribeDisplaysCurrenciesListForSavedState() throws Exception {
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer).accept(CURRENCIES);
    }

    @Test
    public void subscribeDisplaysCurrencySelectionForNullState() throws Exception {
        nullSavedStatePresenter.subscribe();
        verify(displayCurrencySelectionConsumer).accept(0);
    }

    @Test
    public void subscribeDisplaysCurrencySelectionNonNullStateWithoutExtra() throws Exception {
        savedStatePresenter.subscribe();
        verify(displayCurrencySelectionConsumer).accept(0);
    }

    @Test
    public void subscribeListensToClicksAndUpdatesSelection() throws Exception {
        nullSavedStatePresenter.subscribe();
        currencyClicks.onNext(1);
        verify(displayCurrencySelectionConsumer).accept(0);
        verify(displayCurrencySelectionConsumer).accept(1);
    }

    @Test
    public void subscribeHandlesSavesStateWithProperKeyings() throws Exception {
        // Initial config verification
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer).accept(CURRENCIES);
        verify(displayCurrencySelectionConsumer).accept(0);

        // Click our second currency
        currencyClicks.onNext(1);
        verify(displayCurrencySelectionConsumer).accept(1);

        // Save state
        savedStatePresenter.onSaveInstanceState(savedInstanceState);

        // Unsubscribe and resubscribe to verify we have our old position
        savedStatePresenter.unsubscribe();
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer, times(2)).accept(CURRENCIES);
        verify(displayCurrencySelectionConsumer, times(2)).accept(1);
    }

    // onPause -> onResume case
    @Test
    public void subscribeUnsubscribeAndReSubscribeTracksLastCurrencyWhenNotSavingState() throws Exception {
        // Initial config verification
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer).accept(CURRENCIES);
        verify(displayCurrencySelectionConsumer).accept(0);

        // Click our second currency
        currencyClicks.onNext(1);
        verify(displayCurrencySelectionConsumer).accept(1);

        // Unsubscribe and resubscribe to verify we have our old position
        savedStatePresenter.unsubscribe();
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer, times(2)).accept(CURRENCIES);
        verify(displayCurrencySelectionConsumer, times(2)).accept(1);
    }

    @Test
    public void savesStateAndThenSubscribe() throws Exception {
        // Save state
        savedStatePresenter.onSaveInstanceState(savedInstanceState);

        // Initial config verification
        savedStatePresenter.subscribe();
        verify(displayCurrenciesConsumer).accept(CURRENCIES);
        verify(displayCurrencySelectionConsumer).accept(0);

        // Click our second currency
        currencyClicks.onNext(1);
        verify(displayCurrencySelectionConsumer).accept(1);
    }

}