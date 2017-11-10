package co.smartreceipts.android.receipts.editor.pricing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.functions.Consumer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptPricingPresenterTest {

    @Mock
    ReceiptPricingView view;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Receipt receipt;

    @Mock
    Price price;

    @Mock
    Price tax;

    @Mock
    Consumer<Price> displayReceiptPriceConsumer;

    @Mock
    Consumer<Price> displayReceiptTaxConsumer;

    @Mock
    Consumer<Boolean> toggleReceiptTaxFieldVisibilityConsumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(displayReceiptPriceConsumer).when(view).displayReceiptPrice();
        doReturn(displayReceiptTaxConsumer).when(view).displayReceiptTax();
        doReturn(toggleReceiptTaxFieldVisibilityConsumer).when(view).toggleReceiptTaxFieldVisibility();
        when(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)).thenReturn(true);
        when(receipt.getPrice()).thenReturn(price);
        when(receipt.getTax()).thenReturn(tax);
    }

    @Test
    public void subscribeWithTaxesEnabled() throws Exception {
        when(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)).thenReturn(true);
        final ReceiptPricingPresenter presenter = new ReceiptPricingPresenter(view, userPreferenceManager, receipt, null);
        presenter.subscribe();
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true);
    }

    @Test
    public void subscribeWithTaxesDisabled() throws Exception {
        when(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)).thenReturn(false);
        final ReceiptPricingPresenter presenter = new ReceiptPricingPresenter(view, userPreferenceManager, receipt, null);
        presenter.subscribe();
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(false);
    }

    @Test
    public void subscribeWithNullReceipt() throws Exception {
        final ReceiptPricingPresenter presenter = new ReceiptPricingPresenter(view, userPreferenceManager, null, null);
        presenter.subscribe();
        verifyZeroInteractions(displayReceiptPriceConsumer);
        verifyZeroInteractions(displayReceiptTaxConsumer);
    }

    @Test
    public void subscribeWithReceiptAndNullState() throws Exception {
        final ReceiptPricingPresenter presenter = new ReceiptPricingPresenter(view, userPreferenceManager, receipt, null);
        presenter.subscribe();
        verify(displayReceiptPriceConsumer).accept(price);
        verify(displayReceiptTaxConsumer).accept(tax);
    }

    @Test
    public void subscribeWithReceiptAndNonNullState() throws Exception {
        final ReceiptPricingPresenter presenter = new ReceiptPricingPresenter(view, userPreferenceManager, receipt, new Bundle());
        presenter.subscribe();
        verifyZeroInteractions(displayReceiptPriceConsumer);
        verifyZeroInteractions(displayReceiptTaxConsumer);
    }

}