package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.utils.TestLocaleToggler;
import co.smartreceipts.android.utils.TestUtils;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ImmutableNetPriceImplTest {

    private static final PriceCurrency USD_CURRENCY = PriceCurrency.getInstance("USD");
    private static final PriceCurrency EUR_CURRENCY = PriceCurrency.getInstance("EUR");
    private static final ExchangeRate USD_EXCHANGE_RATE = new ExchangeRateBuilderFactory().setBaseCurrency(USD_CURRENCY).build();
    private static final ExchangeRate EUR_EXCHANGE_RATE = new ExchangeRateBuilderFactory().setBaseCurrency(EUR_CURRENCY).build();
    private static final ExchangeRate EUR_TO_USD_EXCHANGE_RATE = new ExchangeRateBuilderFactory().setBaseCurrency(USD_CURRENCY).setRate(EUR_CURRENCY, 1).build();

    ImmutableNetPriceImpl sameCurrencyPrice;
    ImmutableNetPriceImpl differentCurrenciesNoExchangeRatePrice;
    ImmutableNetPriceImpl differentCurrenciesWithExchangeRatePrice;

    @Before
    public void setUp() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        final Price usd1 = new PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(1).setExchangeRate(USD_EXCHANGE_RATE).build();
        final Price eur1 = new PriceBuilderFactory().setCurrency(EUR_CURRENCY).setPrice(1).setExchangeRate(EUR_EXCHANGE_RATE).build();
        final Price eurToUsd1 = new PriceBuilderFactory().setCurrency(EUR_CURRENCY).setPrice(1).setExchangeRate(EUR_TO_USD_EXCHANGE_RATE).build();
        final Price usd2 = new PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(2).setExchangeRate(USD_EXCHANGE_RATE).build();
        sameCurrencyPrice = new ImmutableNetPriceImpl(USD_CURRENCY, Arrays.asList(usd1, usd2));
        differentCurrenciesNoExchangeRatePrice = new ImmutableNetPriceImpl(USD_CURRENCY, Arrays.asList(eur1, usd2));
        differentCurrenciesWithExchangeRatePrice = new ImmutableNetPriceImpl(USD_CURRENCY, Arrays.asList(eurToUsd1, usd2));
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void getPriceAsFloat() {
        assertEquals(3, sameCurrencyPrice.getPriceAsFloat(), TestUtils.EPSILON);
        assertEquals(3, differentCurrenciesWithExchangeRatePrice.getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void getPrice() {
        assertEquals(3, sameCurrencyPrice.getPrice().doubleValue(), TestUtils.EPSILON);
        assertEquals(3, differentCurrenciesWithExchangeRatePrice.getPrice().doubleValue(), TestUtils.EPSILON);
    }

    @Test
    public void getDecimalFormattedPrice() {
        assertEquals("3.00", sameCurrencyPrice.getDecimalFormattedPrice());
        assertEquals("3.00", differentCurrenciesWithExchangeRatePrice.getDecimalFormattedPrice());
    }

    @Test
    public void getCurrencyFormattedPrice() {
        assertEquals("$3.00", sameCurrencyPrice.getCurrencyFormattedPrice());
        assertEquals("EUR1.00; $2.00", differentCurrenciesNoExchangeRatePrice.getCurrencyFormattedPrice());
        assertEquals("$3.00", differentCurrenciesWithExchangeRatePrice.getCurrencyFormattedPrice());
    }

    @Test
    public void getCurrencyCodeFormattedPrice() throws Exception {
        assertEquals("USD3.00", sameCurrencyPrice.getCurrencyCodeFormattedPrice());
        assertEquals("EUR1.00; USD2.00", differentCurrenciesNoExchangeRatePrice.getCurrencyCodeFormattedPrice());
        assertEquals("USD3.00", differentCurrenciesWithExchangeRatePrice.getCurrencyCodeFormattedPrice());
    }

    @Test
    public void getCurrency() {
        assertEquals(USD_CURRENCY, sameCurrencyPrice.getCurrency());
        assertEquals(USD_CURRENCY, differentCurrenciesNoExchangeRatePrice.getCurrency());
        assertEquals(USD_CURRENCY, differentCurrenciesWithExchangeRatePrice.getCurrency());
    }

    @Test
    public void getCurrencyCode() {
        assertEquals(USD_CURRENCY.getCurrencyCode(), sameCurrencyPrice.getCurrencyCode());
        assertEquals(String.format("%s; %s", EUR_CURRENCY.getCurrencyCode(), USD_CURRENCY.getCurrencyCode()) , differentCurrenciesNoExchangeRatePrice.getCurrencyCode());
        assertEquals(String.format("%s; %s", EUR_CURRENCY.getCurrencyCode(), USD_CURRENCY.getCurrencyCode()), differentCurrenciesWithExchangeRatePrice.getCurrencyCode());
    }

    @Test
    public void getCurrencyCodeFormattedNotExchangedPriceTest() {
        assertEquals("$3.00", sameCurrencyPrice.getCurrencyCodeFormattedNotExchangedPrice());
        assertEquals("EUR1.00; USD2.00", differentCurrenciesNoExchangeRatePrice.getCurrencyCodeFormattedNotExchangedPrice());
        assertEquals("EUR1.00; USD2.00", differentCurrenciesWithExchangeRatePrice.getCurrencyCodeFormattedNotExchangedPrice());

    }

    @Test
    public void testToString() {
        assertEquals("$3.00", sameCurrencyPrice.getCurrencyFormattedPrice());
        assertEquals("EUR1.00; $2.00", differentCurrenciesNoExchangeRatePrice.getCurrencyFormattedPrice());
        assertEquals("$3.00", differentCurrenciesWithExchangeRatePrice.getCurrencyFormattedPrice());
    }

    @Test
    public void parcel() throws Exception {
        // Test one
        final Parcel parcel1 = Parcel.obtain();
        sameCurrencyPrice.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        final ImmutableNetPriceImpl parcelPrice1 = ImmutableNetPriceImpl.CREATOR.createFromParcel(parcel1);
        assertNotNull(parcelPrice1);
        assertEquals(sameCurrencyPrice, parcelPrice1);

        // Test two
        final Parcel parcel2 = Parcel.obtain();
        differentCurrenciesNoExchangeRatePrice.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        final ImmutableNetPriceImpl parcelPrice2 = ImmutableNetPriceImpl.CREATOR.createFromParcel(parcel2);
        assertNotNull(parcelPrice2);
        assertEquals(differentCurrenciesNoExchangeRatePrice, parcelPrice2);

        // Test three
        final Parcel parcel3 = Parcel.obtain();
        differentCurrenciesWithExchangeRatePrice.writeToParcel(parcel3, 0);
        parcel3.setDataPosition(0);

        final ImmutableNetPriceImpl parcelPrice3 = ImmutableNetPriceImpl.CREATOR.createFromParcel(parcel3);
        assertNotNull(parcelPrice3);
        assertEquals(differentCurrenciesWithExchangeRatePrice, parcelPrice3);
    }

    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
    @Test
    public void equals() {
        final Price usd1 = new PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(1).setExchangeRate(USD_EXCHANGE_RATE).build();
        final Price usd2 = new PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(2).setExchangeRate(USD_EXCHANGE_RATE).build();
        final Price equalPrice = new ImmutableNetPriceImpl(USD_CURRENCY, Arrays.asList(usd1, usd2));

        Assert.assertEquals(sameCurrencyPrice, sameCurrencyPrice);
        Assert.assertEquals(sameCurrencyPrice, differentCurrenciesWithExchangeRatePrice);
        Assert.assertEquals(sameCurrencyPrice, equalPrice);
        Assert.assertEquals(sameCurrencyPrice, new ImmutablePriceImpl(new BigDecimal(3), USD_CURRENCY, USD_EXCHANGE_RATE));
        assertThat(sameCurrencyPrice, not(equalTo(differentCurrenciesNoExchangeRatePrice)));
        assertThat(sameCurrencyPrice, not(equalTo(new Object())));
        assertThat(sameCurrencyPrice, not(equalTo(mock(Distance.class))));
        assertThat(sameCurrencyPrice, not(equalTo(new ImmutablePriceImpl(new BigDecimal(0), USD_CURRENCY, USD_EXCHANGE_RATE))));
        assertThat(sameCurrencyPrice, not(equalTo(new ImmutablePriceImpl(new BigDecimal(3), PriceCurrency.getInstance("EUR"), USD_EXCHANGE_RATE))));
    }

}