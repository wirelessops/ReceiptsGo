package co.smartreceipts.android.receipts.editor.currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.fragments.ReceiptInputCache;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptCurrencyCodeSupplierTest {

    @Mock
    Trip trip;

    @Mock
    ReceiptInputCache receiptInputCache;

    @Mock
    Receipt receipt;

    @Mock
    Price price;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(receipt.getPrice()).thenReturn(price);
    }

    @Test
    public void get() throws Exception {
        when(trip.getDefaultCurrencyCode()).thenReturn("trip");
        when(price.getCurrencyCode()).thenReturn("receipt");
        final ReceiptCurrencyCodeSupplier nullReceiptSupplier = new ReceiptCurrencyCodeSupplier(trip, receiptInputCache, null);
        final ReceiptCurrencyCodeSupplier validReceiptSupplier = new ReceiptCurrencyCodeSupplier(trip, receiptInputCache, receipt);
        assertEquals(nullReceiptSupplier.get(), "trip");
        assertEquals(validReceiptSupplier.get(), "receipt");

        when(receiptInputCache.getCachedCurrency()).thenReturn("cache");
        final ReceiptCurrencyCodeSupplier nullReceiptSupplierWithCachedCurrency = new ReceiptCurrencyCodeSupplier(trip, receiptInputCache, null);
        assertEquals(nullReceiptSupplierWithCachedCurrency.get(), "cache");
    }

}