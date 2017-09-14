package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.utils.TestUtils;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ImmutablePriceImplTest {

    private static final float PRICE_FLOAT = 1.2511f;
    private static final BigDecimal PRICE = new BigDecimal(PRICE_FLOAT);
    private static final PriceCurrency CURRENCY = PriceCurrency.getInstance("USD");
    private static final ExchangeRate EXCHANGE_RATE = new ExchangeRateBuilderFactory().setBaseCurrency(CURRENCY).build();

    ImmutablePriceImpl price;
    ImmutablePriceImpl priceWith3DigitsOfPrecision;

    @Before
    public void setUp() throws Exception {
        price = new ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE);
        priceWith3DigitsOfPrecision = new ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE, 3);
    }

    @Test
    public void getPriceAsFloat() throws Exception {
        assertEquals(PRICE_FLOAT, price.getPriceAsFloat(), TestUtils.EPSILON);
        assertEquals(PRICE_FLOAT, priceWith3DigitsOfPrecision.getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void getPrice() throws Exception {
        assertEquals(PRICE.doubleValue(), price.getPrice().doubleValue(), TestUtils.EPSILON);
        assertEquals(PRICE.doubleValue(), priceWith3DigitsOfPrecision.getPrice().doubleValue(), TestUtils.EPSILON);
    }

    @Test
    public void getDecimalFormattedPrice() throws Exception {
        assertEquals("1.25", price.getDecimalFormattedPrice());
        assertEquals("1.251", priceWith3DigitsOfPrecision.getDecimalFormattedPrice());
    }

    @Test
    public void getCurrencyFormattedPrice() throws Exception {
        assertEquals("$1.25", price.getCurrencyFormattedPrice());
        assertEquals("$1.251", priceWith3DigitsOfPrecision.getCurrencyFormattedPrice());
    }

    @Test
    public void getCurrencyCodeFormattedPrice() throws Exception {
        assertEquals("USD1.25", price.getCurrencyCodeFormattedPrice());
        assertEquals("USD1.251", priceWith3DigitsOfPrecision.getCurrencyCodeFormattedPrice());
    }

    @Test
    public void getCurrency() throws Exception {
        assertEquals(CURRENCY, price.getCurrency());
        assertEquals(CURRENCY, priceWith3DigitsOfPrecision.getCurrency());
    }

    @Test
    public void getCurrencyCode() throws Exception {
        assertEquals("USD", price.getCurrencyCode());
        assertEquals("USD", priceWith3DigitsOfPrecision.getCurrencyCode());
    }

    @Test
    public void getExchangeRate() throws Exception {
        assertEquals(EXCHANGE_RATE, price.getExchangeRate());
        assertEquals(EXCHANGE_RATE, priceWith3DigitsOfPrecision.getExchangeRate());
    }

    @Test
    public void testToString() {
        assertEquals("$1.25", price.toString());
        assertEquals("$1.251", priceWith3DigitsOfPrecision.toString());
    }

    @Test
    public void parcel() throws Exception {
        final Parcel parcel = Parcel.obtain();
        price.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final ImmutablePriceImpl parcelPrice = ImmutablePriceImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parcelPrice);
        assertEquals(price, parcelPrice);
    }

    @Test
    public void equals() {
        Assert.assertEquals(price, price);
        Assert.assertEquals(price, new ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE));
        assertThat(price, not(equalTo(new Object())));
        assertThat(price, not(equalTo(mock(Distance.class))));
        assertThat(price, not(equalTo(new ImmutablePriceImpl(new BigDecimal(0), CURRENCY, EXCHANGE_RATE))));
        assertThat(price, not(equalTo(new ImmutablePriceImpl(PRICE, PriceCurrency.getInstance("EUR"), EXCHANGE_RATE))));
    }

}