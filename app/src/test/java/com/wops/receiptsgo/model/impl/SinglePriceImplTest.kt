package com.wops.receiptsgo.model.impl

import android.os.Parcel
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.factory.ExchangeRateBuilderFactory
import com.wops.receiptsgo.utils.testParcel
import org.joda.money.CurrencyUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(RobolectricTestRunner::class)
class SinglePriceImplTest {

    companion object {

        private const val PRICE_FLOAT = 1.2511f
        private val PRICE = BigDecimal(PRICE_FLOAT.toDouble())
        private val PRICE_NEGATIVE = BigDecimal(PRICE_FLOAT * -1.0)
        private val CURRENCY1 = CurrencyUnit.USD // currency with 2 decimal places
        private val CURRENCY2 = CurrencyUnit.of("BHD") // currency with 3 decimal places
        private val CURRENCY3 = CurrencyUnit.JPY // currency with 0 decimal places
        private val EXCHANGE_RATE = ExchangeRateBuilderFactory().setBaseCurrency(CURRENCY1).build()
    }

    private lateinit var price1: SinglePriceImpl
    private lateinit var price2: SinglePriceImpl
    private lateinit var price3: SinglePriceImpl
    private lateinit var priceNegative: SinglePriceImpl

    @Before
    fun setUp() {
        price1 = SinglePriceImpl(PRICE, CURRENCY1, EXCHANGE_RATE)
        price2 = SinglePriceImpl(PRICE, CURRENCY2, EXCHANGE_RATE)
        price3 = SinglePriceImpl(PRICE, CURRENCY3, EXCHANGE_RATE)
        priceNegative = SinglePriceImpl(PRICE_NEGATIVE, CURRENCY1, EXCHANGE_RATE)
    }

    @Test
    fun getPriceAsFloat() {
        assertEquals(1.25f, price1.priceAsFloat)
        assertEquals(1.251f, price2.priceAsFloat)
        assertEquals(1f, price3.priceAsFloat)
        assertEquals(-1.25f, priceNegative.priceAsFloat)
    }

    @Test
    fun getMoney() {
        assertEquals(PRICE.setScale(2, RoundingMode.HALF_EVEN), price1.money.amount)
        assertEquals(PRICE.setScale(3, RoundingMode.HALF_EVEN), price2.money.amount)
        assertEquals(PRICE.setScale(0, RoundingMode.HALF_EVEN), price3.money.amount)
        assertEquals(PRICE_NEGATIVE.setScale(2, RoundingMode.HALF_EVEN), priceNegative.money.amount)
    }

    @Test
    fun getDecimalFormattedPrice() {
        assertEquals("1.25", price1.decimalFormattedPrice)
        assertEquals("1.251", price2.decimalFormattedPrice)
        assertEquals("1", price3.decimalFormattedPrice)
        assertEquals("-1.25", priceNegative.decimalFormattedPrice)
    }

    @Test
    fun getCurrencyFormattedPrice() {
        assertEquals("$1.25", price1.currencyFormattedPrice)
        assertEquals("BHD1.251", price2.currencyFormattedPrice)
        assertEquals("¥1", price3.currencyFormattedPrice)
        assertEquals("$-1.25", priceNegative.currencyFormattedPrice)
    }

    @Test
    fun getCurrencyCodeFormattedPrice() {
        assertEquals("USD 1.25", price1.currencyCodeFormattedPrice)
        assertEquals("BHD 1.251", price2.currencyCodeFormattedPrice)
        assertEquals("JPY 1", price3.currencyCodeFormattedPrice)
        assertEquals("USD -1.25", priceNegative.currencyCodeFormattedPrice)
    }

    @Test
    fun getCurrency() {
        assertEquals(CURRENCY1, price1.currency)
        assertEquals(CURRENCY1, priceNegative.currency)
        assertEquals(CURRENCY2, price2.currency)
        assertEquals(CURRENCY3, price3.currency)
    }

    @Test
    fun getCurrencyCode() {
        assertEquals("USD", price1.currencyCode)
        assertEquals("USD", priceNegative.currencyCode)
        assertEquals("BHD", price2.currencyCode)
        assertEquals("JPY", price3.currencyCode)
    }

    @Test
    fun isSingleCurrency() {
        assertEquals(true, price1.isSingleCurrency)
        assertEquals(true, price2.isSingleCurrency)
        assertEquals(true, price3.isSingleCurrency)
        assertEquals(true, priceNegative.isSingleCurrency)
    }

    @Test
    fun getExchangeRate() {
        assertEquals(EXCHANGE_RATE, price1.exchangeRate)
        assertEquals(EXCHANGE_RATE, price2.exchangeRate)
        assertEquals(EXCHANGE_RATE, price3.exchangeRate)
        assertEquals(EXCHANGE_RATE, priceNegative.exchangeRate)
    }

    @Test
    fun testToString() {
        assertEquals("$1.25", price1.toString())
        assertEquals("$-1.25", priceNegative.toString())
        assertEquals("BHD1.251", price2.toString())
        assertEquals("¥1", price3.toString())
    }

    @Test
    fun parcel() {
        val parcel = Parcel.obtain()
        price1.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val parcelPrice = SinglePriceImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(parcelPrice)
        assertEquals(price1, parcelPrice)
    }

    @Test
    fun equals() {
        assertEquals(price1, price1)
        assertEquals(price1, SinglePriceImpl(PRICE, CURRENCY1, EXCHANGE_RATE))

        assertNotEquals(Any(), price1)
        assertNotEquals(priceNegative, price1)
        assertNotEquals(mock(Distance::class.java), price1)
        assertNotEquals(SinglePriceImpl(BigDecimal.ZERO, CURRENCY1, EXCHANGE_RATE), price1)
        assertNotEquals(SinglePriceImpl(PRICE, CurrencyUnit.EUR, EXCHANGE_RATE), price1)
    }

    @Test
    fun parcelEquality() {
        val priceFromParcel = price1.testParcel()

        assertNotSame(priceFromParcel, price1)
        assertEquals(price1, priceFromParcel)
    }
}