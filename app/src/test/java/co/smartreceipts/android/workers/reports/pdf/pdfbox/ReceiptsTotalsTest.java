package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptsTotalsTest {

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Trip trip;

    @Mock
    Receipt reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt;

    @Mock
    Distance distance1, distance2;

    Price priceTwoEurThatConvertsToOneUsd, priceFiveUsd, priceTenUsd;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        priceTwoEurThatConvertsToOneUsd = new PriceBuilderFactory().setPrice(2).setCurrency("EUR").setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency("EUR").setRate("USD", "0.5").build()).build();
        priceFiveUsd = new PriceBuilderFactory().setPrice(5).setCurrency("USD").build();
        priceTenUsd = new PriceBuilderFactory().setPrice(10).setCurrency("USD").build();
        when(trip.getTripCurrency()).thenReturn(PriceCurrency.getInstance("USD"));
        when(reimbursableReceipt1.getPrice()).thenReturn(priceFiveUsd);
        when(reimbursableReceipt1.getTax()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(reimbursableReceipt1.isReimbursable()).thenReturn(true);
        when(reimbursableReceipt2.getPrice()).thenReturn(priceTenUsd);
        when(reimbursableReceipt2.getTax()).thenReturn(priceFiveUsd);
        when(reimbursableReceipt2.isReimbursable()).thenReturn(true);
        when(nonReimbursableReceipt.getPrice()).thenReturn(priceFiveUsd);
        when(nonReimbursableReceipt.getTax()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(nonReimbursableReceipt.isReimbursable()).thenReturn(false);
        when(distance1.getPrice()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(distance2.getPrice()).thenReturn(priceTwoEurThatConvertsToOneUsd);
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(6).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(21).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(21).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(21).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(9).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(6).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(6).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(21).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(23).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(23).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(9).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(6).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(17).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(17).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndNotOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(20).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(7).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(27).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(27).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(21).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndNotOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(13).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(7).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(20).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(20).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(15).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndNotOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(20).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(7).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(27).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(29).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(23).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndNotOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);
        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(13).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice(7).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(20).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(22).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(17).setCurrency("USD").build());
    }

}