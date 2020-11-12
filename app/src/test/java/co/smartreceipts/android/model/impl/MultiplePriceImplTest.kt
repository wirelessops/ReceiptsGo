package co.smartreceipts.android.model.impl

import android.os.Parcel
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.TestUtils
import org.joda.money.CurrencyUnit
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.*

@RunWith(RobolectricTestRunner::class)
class MultiplePriceImplTest {
    companion object {
        private val USD_CURRENCY = CurrencyUnit.USD
        private val EUR_CURRENCY = CurrencyUnit.EUR
        private val JPY_CURRENCY = CurrencyUnit.JPY // currency with 0 decimal places

        private val USD_EXCHANGE_RATE =
            ExchangeRateBuilderFactory().setBaseCurrency(USD_CURRENCY).setRate(EUR_CURRENCY, BigDecimal(0.5)).build()
        private val EUR_EXCHANGE_RATE =
            ExchangeRateBuilderFactory().setBaseCurrency(EUR_CURRENCY).setRate(USD_CURRENCY, BigDecimal(2.0)).build()
        private val JPY_EXCHANGE_RATE = ExchangeRateBuilderFactory().setBaseCurrency(JPY_CURRENCY).build()
    }


    private lateinit var sameCurrencyPrice: MultiplePriceImpl
    private lateinit var sameCurrencyWithNegativePrice: MultiplePriceImpl
    private lateinit var differentCurrenciesNoExchangeRatePrice: MultiplePriceImpl
    private lateinit var differentCurrenciesWithExchangeRatePrice: MultiplePriceImpl

    @Before
    @Throws(Exception::class)
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)

        val priceUsd1 = SinglePriceImpl(BigDecimal.ONE, USD_CURRENCY, USD_EXCHANGE_RATE)
        val priceUsd2 = SinglePriceImpl(BigDecimal(2.0), USD_CURRENCY, USD_EXCHANGE_RATE)
        val priceUsd2Negative = SinglePriceImpl(BigDecimal(-2.0), USD_CURRENCY, USD_EXCHANGE_RATE)

        val priceEur1 = SinglePriceImpl(BigDecimal.ONE, EUR_CURRENCY, EUR_EXCHANGE_RATE)

        val priceJpu1 = SinglePriceImpl(BigDecimal.ONE, JPY_CURRENCY, JPY_EXCHANGE_RATE)

        sameCurrencyPrice = MultiplePriceImpl(USD_CURRENCY, listOf(priceUsd1, priceUsd2))
        sameCurrencyWithNegativePrice = MultiplePriceImpl(USD_CURRENCY, listOf(priceUsd1, priceUsd2Negative))
        differentCurrenciesWithExchangeRatePrice = MultiplePriceImpl(USD_CURRENCY, listOf(priceUsd1, priceUsd2, priceEur1))
        differentCurrenciesNoExchangeRatePrice = MultiplePriceImpl(USD_CURRENCY, listOf(priceUsd1, priceEur1, priceJpu1))
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getPriceAsFloat() {
        assertEquals(3f, sameCurrencyPrice.priceAsFloat, TestUtils.EPSILON)
        assertEquals(-1f, sameCurrencyWithNegativePrice.priceAsFloat, TestUtils.EPSILON)
        assertEquals(5f, differentCurrenciesWithExchangeRatePrice.priceAsFloat, TestUtils.EPSILON)
        assertEquals(4f, differentCurrenciesNoExchangeRatePrice.priceAsFloat, TestUtils.EPSILON)
    }

    @Test
    fun getPrice() {
        assertEquals(3.0, sameCurrencyPrice.price.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(-1.0, sameCurrencyWithNegativePrice.price.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(5.0, differentCurrenciesWithExchangeRatePrice.price.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(4.0, differentCurrenciesNoExchangeRatePrice.price.toDouble(), TestUtils.EPSILON.toDouble())
    }

    @Test
    fun getDecimalFormattedPrice() {
        assertEquals("3.00", sameCurrencyPrice.decimalFormattedPrice)
        assertEquals("-1.00", sameCurrencyWithNegativePrice.decimalFormattedPrice)
        assertEquals("5.00", differentCurrenciesWithExchangeRatePrice.decimalFormattedPrice)
        assertEquals("JPY 1; USD 3.00", differentCurrenciesNoExchangeRatePrice.decimalFormattedPrice)
    }

    @Test
    fun getCurrencyFormattedPrice() {
        assertEquals("$3.00", sameCurrencyPrice.currencyFormattedPrice)
        assertEquals("$-1.00", sameCurrencyWithNegativePrice.currencyFormattedPrice)
        assertEquals("$5.00", differentCurrenciesWithExchangeRatePrice.currencyFormattedPrice)
        assertEquals("JPY1; $3.00", differentCurrenciesNoExchangeRatePrice.currencyFormattedPrice)
    }

    @Test
    fun getCurrencyCodeFormattedPrice() {
        assertEquals("USD 3.00", sameCurrencyPrice.currencyCodeFormattedPrice)
        assertEquals("USD -1.00", sameCurrencyWithNegativePrice.currencyCodeFormattedPrice)
        assertEquals("EUR 1.00; USD 3.00", differentCurrenciesWithExchangeRatePrice.currencyCodeFormattedPrice)
        assertEquals("JPY 1; EUR 1.00; USD 1.00", differentCurrenciesNoExchangeRatePrice.currencyCodeFormattedPrice)
    }

    @Test
    fun getCurrency() {
        assertEquals(USD_CURRENCY, sameCurrencyPrice.currency)
        assertEquals(USD_CURRENCY, sameCurrencyWithNegativePrice.currency)
        assertEquals(USD_CURRENCY, differentCurrenciesNoExchangeRatePrice.currency)
        assertEquals(USD_CURRENCY, differentCurrenciesWithExchangeRatePrice.currency)
    }

    @Test
    fun getCurrencyCode() {
        assertEquals(USD_CURRENCY.code, sameCurrencyPrice.currencyCode)
        assertEquals(USD_CURRENCY.code, sameCurrencyWithNegativePrice.currencyCode)
        assertEquals(String.format("%s; %s", EUR_CURRENCY.code, USD_CURRENCY.code), differentCurrenciesWithExchangeRatePrice.currencyCode)
        assertEquals(
            String.format("%s; %s; %s", JPY_CURRENCY.code, EUR_CURRENCY.code, USD_CURRENCY.code),
            differentCurrenciesNoExchangeRatePrice.currencyCode
        )
    }

    @Test
    fun isSingleCurrency() {
        assertEquals(true, sameCurrencyPrice.isSingleCurrency)
        assertEquals(true, sameCurrencyWithNegativePrice.isSingleCurrency)
        assertEquals(false, differentCurrenciesNoExchangeRatePrice.isSingleCurrency)
        assertEquals(false, differentCurrenciesWithExchangeRatePrice.isSingleCurrency)
    }

    @Test
    fun testToString() {
        assertEquals("$3.00", sameCurrencyPrice.currencyFormattedPrice)
        assertEquals("$-1.00", sameCurrencyWithNegativePrice.currencyFormattedPrice)
        assertEquals("$5.00", differentCurrenciesWithExchangeRatePrice.currencyFormattedPrice)
        assertEquals("JPY1; $3.00", differentCurrenciesNoExchangeRatePrice.currencyFormattedPrice)
    }

    @Test
    fun parcel() {
        // Test one
        val parcel1 = Parcel.obtain()
        sameCurrencyPrice.writeToParcel(parcel1, 0)
        parcel1.setDataPosition(0)

        val parcelPrice1 = MultiplePriceImpl.CREATOR.createFromParcel(parcel1)
        assertNotNull(parcelPrice1)
        assertEquals(sameCurrencyPrice, parcelPrice1)

        // Test two
        val parcel2 = Parcel.obtain()
        differentCurrenciesNoExchangeRatePrice.writeToParcel(parcel2, 0)
        parcel2.setDataPosition(0)

        val parcelPrice2 = MultiplePriceImpl.CREATOR.createFromParcel(parcel2)
        assertNotNull(parcelPrice2)
        assertEquals(differentCurrenciesNoExchangeRatePrice, parcelPrice2)

        // Test three
        val parcel3 = Parcel.obtain()
        differentCurrenciesWithExchangeRatePrice.writeToParcel(parcel3, 0)
        parcel3.setDataPosition(0)

        val parcelPrice3 = MultiplePriceImpl.CREATOR.createFromParcel(parcel3)
        assertNotNull(parcelPrice3)
        assertEquals(differentCurrenciesWithExchangeRatePrice, parcelPrice3)

        // Test negative
        val parcelNegative = Parcel.obtain()
        sameCurrencyWithNegativePrice.writeToParcel(parcelNegative, 0)
        parcelNegative.setDataPosition(0)

        val parcelPriceNegative = MultiplePriceImpl.CREATOR.createFromParcel(parcelNegative)
        assertNotNull(parcelPriceNegative)
        assertEquals(sameCurrencyWithNegativePrice, parcelPriceNegative)
    }

    @Test
    fun equals() {
        val usd1 = SinglePriceImpl(BigDecimal.ONE, USD_CURRENCY, USD_EXCHANGE_RATE)
        val usd2 = SinglePriceImpl(BigDecimal(2), USD_CURRENCY, USD_EXCHANGE_RATE)
        val usd0 = SinglePriceImpl(BigDecimal.ZERO, USD_CURRENCY, USD_EXCHANGE_RATE)
        val eur3 = SinglePriceImpl(BigDecimal(3), CurrencyUnit.EUR, EUR_EXCHANGE_RATE)
        val eur1 = SinglePriceImpl(BigDecimal.ONE, CurrencyUnit.EUR, EUR_EXCHANGE_RATE)

        val equalPrice = MultiplePriceImpl(USD_CURRENCY, listOf(usd1, usd2))

        val equalPriceWithEur = MultiplePriceImpl(USD_CURRENCY, listOf(usd1, eur1))


        assertEquals(sameCurrencyPrice, sameCurrencyPrice)
        assertEquals(sameCurrencyPrice, equalPriceWithEur)
        assertEquals(sameCurrencyPrice, equalPrice)
        assertEquals(
            sameCurrencyPrice,
            SinglePriceImpl(BigDecimal(3), USD_CURRENCY, USD_EXCHANGE_RATE)
        )

        assertNotEquals(differentCurrenciesNoExchangeRatePrice, sameCurrencyPrice)
        assertNotEquals(sameCurrencyWithNegativePrice, sameCurrencyPrice)
        assertNotEquals(Any(), sameCurrencyPrice)
        assertNotEquals(usd0, sameCurrencyPrice)
        assertNotEquals(eur3, sameCurrencyPrice)
        assertNotEquals(mock(Distance::class.java), sameCurrencyPrice)
    }

    @Test
    fun localeBasedFormattingTest() {
        val usd1 = SinglePriceImpl(BigDecimal(1000), USD_CURRENCY, USD_EXCHANGE_RATE)
        val usd2 = SinglePriceImpl(BigDecimal(2.5023), USD_CURRENCY, USD_EXCHANGE_RATE)

        TestLocaleToggler.setDefaultLocale(Locale.US)
        val multiplePriceUs = MultiplePriceImpl(USD_CURRENCY, listOf(usd1, usd2))
        assertEquals("1,002.50", multiplePriceUs.decimalFormattedPrice)
        assertEquals("USD 1,002.50", multiplePriceUs.currencyCodeFormattedPrice)
        assertEquals("$1,002.50", multiplePriceUs.currencyFormattedPrice)

        TestLocaleToggler.setDefaultLocale(Locale.FRANCE)
        val multiplePriceFrance = MultiplePriceImpl(USD_CURRENCY, listOf(usd1, usd2))
        assertEquals("1 002,50", multiplePriceFrance.decimalFormattedPrice)
        assertEquals("USD 1 002,50", multiplePriceFrance.currencyCodeFormattedPrice)
        assertEquals("USD1 002,50", multiplePriceFrance.currencyFormattedPrice)
    }

}