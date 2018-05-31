package co.smartreceipts.android.model.impl

import android.os.Parcel
import android.os.Parcelable
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.TestUtils
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ImmutableLegacyNetPriceImplTest {

    companion object {

        private val USD_CURRENCY = PriceCurrency.getInstance("USD")
        private val EUR_CURRENCY = PriceCurrency.getInstance("EUR")
        private val USD_EXCHANGE_RATE =
            ExchangeRateBuilderFactory().setBaseCurrency(USD_CURRENCY).build()
        private val EUR_EXCHANGE_RATE =
            ExchangeRateBuilderFactory().setBaseCurrency(EUR_CURRENCY).build()
        private val EUR_TO_USD_EXCHANGE_RATE =
            ExchangeRateBuilderFactory().setBaseCurrency(USD_CURRENCY).setRate(EUR_CURRENCY, 1.0)
                .build()
    }

    private lateinit var sameCurrencyPrice: ImmutableLegacyNetPriceImpl
    private lateinit var differentCurrenciesNoExchangeRatePrice: ImmutableLegacyNetPriceImpl
    private lateinit var differentCurrenciesWithExchangeRatePrice: ImmutableLegacyNetPriceImpl

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        val usd1 = PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(1.0)
            .setExchangeRate(USD_EXCHANGE_RATE).build()
        val eur1 = PriceBuilderFactory().setCurrency(EUR_CURRENCY).setPrice(1.0)
            .setExchangeRate(EUR_EXCHANGE_RATE).build()
        val eurToUsd1 = PriceBuilderFactory().setCurrency(EUR_CURRENCY).setPrice(1.0)
            .setExchangeRate(EUR_TO_USD_EXCHANGE_RATE).build()
        val usd2 = PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(2.0)
            .setExchangeRate(USD_EXCHANGE_RATE).build()
        sameCurrencyPrice = ImmutableLegacyNetPriceImpl(Arrays.asList(usd1, usd2))
        differentCurrenciesNoExchangeRatePrice =
                ImmutableLegacyNetPriceImpl(Arrays.asList(eur1, usd2))
        differentCurrenciesWithExchangeRatePrice =
                ImmutableLegacyNetPriceImpl(Arrays.asList(eurToUsd1, usd2))
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getPriceAsFloat() {
        assertEquals(3f, sameCurrencyPrice.priceAsFloat, TestUtils.EPSILON)
        assertEquals(3f, differentCurrenciesWithExchangeRatePrice.priceAsFloat, TestUtils.EPSILON)
    }

    @Test
    fun getPrice() {
        assertEquals(3.0, sameCurrencyPrice.price.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(
            3.0,
            differentCurrenciesWithExchangeRatePrice.price.toDouble(),
            TestUtils.EPSILON.toDouble()
        )
    }

    @Test
    fun getDecimalFormattedPrice() {
        assertEquals("3.00", sameCurrencyPrice.decimalFormattedPrice)
        assertEquals("3.00", differentCurrenciesWithExchangeRatePrice.decimalFormattedPrice)
    }

    @Test
    fun getCurrencyFormattedPrice() {
        assertEquals("$3.00", sameCurrencyPrice.currencyFormattedPrice)
        assertEquals("EUR1.00; $2.00", differentCurrenciesNoExchangeRatePrice.currencyFormattedPrice)
        assertEquals("EUR1.00; $2.00", differentCurrenciesWithExchangeRatePrice.currencyFormattedPrice)
    }

    @Test
    fun getCurrencyCodeFormattedPrice() {
        assertEquals("USD3.00", sameCurrencyPrice.currencyCodeFormattedPrice)
        assertEquals("EUR1.00; USD2.00", differentCurrenciesNoExchangeRatePrice.currencyCodeFormattedPrice)
        assertEquals("EUR1.00; USD2.00", differentCurrenciesWithExchangeRatePrice.currencyCodeFormattedPrice)
    }

    @Test
    fun getCurrency() {
        assertEquals(USD_CURRENCY, sameCurrencyPrice.currency)
        assertEquals(PriceCurrency.MIXED_CURRENCY, differentCurrenciesNoExchangeRatePrice.currency)
        assertEquals(PriceCurrency.MIXED_CURRENCY, differentCurrenciesWithExchangeRatePrice.currency)
    }

    @Test
    fun getCurrencyCode() {
        assertEquals(USD_CURRENCY.currencyCode, sameCurrencyPrice.currencyCode)
        assertEquals(PriceCurrency.MIXED_CURRENCY.currencyCode, differentCurrenciesNoExchangeRatePrice.currencyCode)
        assertEquals(PriceCurrency.MIXED_CURRENCY.currencyCode, differentCurrenciesWithExchangeRatePrice.currencyCode)
    }

    @Test
    fun getCurrencyCodeCount() {
        assertEquals(1, sameCurrencyPrice.currencyCodeCount)
        assertEquals(2, differentCurrenciesNoExchangeRatePrice.currencyCodeCount)
        assertEquals(2, differentCurrenciesWithExchangeRatePrice.currencyCodeCount)
    }

    @Test
    fun testToString() {
        assertEquals("$3.00", sameCurrencyPrice.currencyFormattedPrice)
        assertEquals("EUR1.00; $2.00", differentCurrenciesNoExchangeRatePrice.currencyFormattedPrice)
        assertEquals("EUR1.00; $2.00", differentCurrenciesWithExchangeRatePrice.currencyFormattedPrice)
    }

    @Test
    fun parcel() {
        // Test one
        val parcel1 = Parcel.obtain()
        sameCurrencyPrice.writeToParcel(parcel1, 0)
        parcel1.setDataPosition(0)

        val parcelPrice1 = ImmutableLegacyNetPriceImpl.CREATOR.createFromParcel(parcel1)
        assertNotNull(parcelPrice1)
        assertEquals(sameCurrencyPrice, parcelPrice1)

        // Test two
        val parcel2 = Parcel.obtain()
        differentCurrenciesNoExchangeRatePrice.writeToParcel(parcel2, 0)
        parcel2.setDataPosition(0)

        val parcelPrice2 = ImmutableLegacyNetPriceImpl.CREATOR.createFromParcel(parcel2)
        assertNotNull(parcelPrice2)
        assertEquals(differentCurrenciesNoExchangeRatePrice, parcelPrice2)

        // Test three
        val parcel3 = Parcel.obtain()
        differentCurrenciesWithExchangeRatePrice.writeToParcel(parcel3, 0)
        parcel3.setDataPosition(0)

        val parcelPrice3 = ImmutableLegacyNetPriceImpl.CREATOR.createFromParcel(parcel3)
        assertNotNull(parcelPrice3)
        assertEquals(differentCurrenciesWithExchangeRatePrice, parcelPrice3)
    }

    @Test
    fun equals() {
        val usd1 = PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(1.0).setExchangeRate(USD_EXCHANGE_RATE).build()
        val usd2 = PriceBuilderFactory().setCurrency(USD_CURRENCY).setPrice(2.0).setExchangeRate(USD_EXCHANGE_RATE).build()
        val equalPrice = ImmutableNetPriceImpl(USD_CURRENCY, Arrays.asList(usd1, usd2))

        Assert.assertEquals(sameCurrencyPrice, sameCurrencyPrice)
        Assert.assertEquals(sameCurrencyPrice, equalPrice)
        Assert.assertEquals(sameCurrencyPrice, ImmutablePriceImpl(BigDecimal(3), USD_CURRENCY, USD_EXCHANGE_RATE))
        assertThat(sameCurrencyPrice, not(equalTo(differentCurrenciesNoExchangeRatePrice)))
        assertThat(sameCurrencyPrice, not(equalTo(differentCurrenciesWithExchangeRatePrice)))
        assertThat(sameCurrencyPrice, not(equalTo(Any())))
        assertThat<Parcelable>(sameCurrencyPrice, not<Parcelable>(equalTo<Parcelable>(mock(Distance::class.java))))
        assertThat(
            sameCurrencyPrice,
            not(
                equalTo<AbstractPriceImpl>(
                    ImmutablePriceImpl(
                        BigDecimal(0),
                        USD_CURRENCY,
                        USD_EXCHANGE_RATE
                    )
                )
            )
        )
        assertThat(
            sameCurrencyPrice,
            not(
                equalTo<AbstractPriceImpl>(
                    ImmutablePriceImpl(
                        BigDecimal(3),
                        PriceCurrency.getInstance("EUR"),
                        USD_EXCHANGE_RATE
                    )
                )
            )
        )
    }
}