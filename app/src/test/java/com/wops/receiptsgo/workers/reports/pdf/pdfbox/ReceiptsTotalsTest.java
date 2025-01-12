package com.wops.receiptsgo.workers.reports.pdf.pdfbox;

import org.joda.money.CurrencyUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;

import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.ExchangeRateBuilderFactory;
import com.wops.receiptsgo.model.factory.PriceBuilderFactory;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;

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

    Price priceTwoEurThatConvertsToOneUsd, priceFiveUsd, priceTenUsd, priceOneUsd;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        priceTwoEurThatConvertsToOneUsd = new PriceBuilderFactory().setPrice(2).setCurrency("EUR").setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency("EUR").setRate("USD", "0.5").build()).build();
        priceFiveUsd = new PriceBuilderFactory().setPrice(5).setCurrency("USD").build();
        priceTenUsd = new PriceBuilderFactory().setPrice(10).setCurrency("USD").build();
        priceOneUsd = new PriceBuilderFactory().setPrice(1).setCurrency("USD").build();

        when(trip.getTripCurrency()).thenReturn(CurrencyUnit.USD);

        when(reimbursableReceipt1.getPrice()).thenReturn(priceFiveUsd);
        when(reimbursableReceipt1.getTax()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(reimbursableReceipt1.getTax2()).thenReturn(priceOneUsd);
        when(reimbursableReceipt1.isReimbursable()).thenReturn(true);

        when(reimbursableReceipt2.getPrice()).thenReturn(priceTenUsd);
        when(reimbursableReceipt2.getTax()).thenReturn(priceFiveUsd);
        when(reimbursableReceipt2.getTax2()).thenReturn(priceOneUsd);
        when(reimbursableReceipt2.isReimbursable()).thenReturn(true);

        when(nonReimbursableReceipt.getPrice()).thenReturn(priceFiveUsd);
        when(nonReimbursableReceipt.getTax()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(nonReimbursableReceipt.getTax2()).thenReturn(priceOneUsd);
        when(nonReimbursableReceipt.isReimbursable()).thenReturn(false);

        when(distance1.getPrice()).thenReturn(priceTwoEurThatConvertsToOneUsd);
        when(distance2.getPrice()).thenReturn(priceTwoEurThatConvertsToOneUsd);
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1)).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice((5 - 1 - 1) + (10 - 5 - 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + 2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + 2).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice((5 - 1 - 1) + (10 - 5 - 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 2).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndNotOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1) + (1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + (5 + 1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + (5 + 1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1)).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndNotOnlyIncludingReimbursableAndNoDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Collections.emptyList(), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice((5 - 1 - 1) + (10 - 5 - 1) + (5 - 1 - 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1) + (1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(0).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPreTaxPriceAndNotOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(true);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1) + (1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + (5 + 1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + (5 + 1 + 1) + 2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice((5 + 1 + 1) + (10 + 5 + 1) + 2).setCurrency("USD").build());
    }

    @Test
    public void calculateReceiptTotalsWithPostTaxPriceAndNotOnlyIncludingReimbursableAndDistances() {
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);

        final ReceiptsTotals receiptsTotals = new ReceiptsTotals(trip, Arrays.asList(reimbursableReceipt1, reimbursableReceipt2, nonReimbursableReceipt), Arrays.asList(distance1, distance2), userPreferenceManager);

        assertEquals(receiptsTotals.getReceiptsWithOutTaxPrice(), new PriceBuilderFactory().setPrice((5 - 1 - 1) + (10 - 5 - 1) + (5 - 1 - 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getTaxPrice(), new PriceBuilderFactory().setPrice((1 + 1) + (5 + 1) + (1 + 1)).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReceiptsWithTaxPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5).setCurrency("USD").build());
        assertEquals(receiptsTotals.getDistancePrice(), new PriceBuilderFactory().setPrice(2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 5 + 2).setCurrency("USD").build());
        assertEquals(receiptsTotals.getReimbursableGrandTotalPrice(), new PriceBuilderFactory().setPrice(5 + 10 + 2).setCurrency("USD").build());
    }

}