package co.smartreceipts.android.model.impl

import android.os.Parcel
import android.os.Parcelable
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory
import co.smartreceipts.android.utils.TestUtils
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class ImmutablePriceImplTest {

    companion object {

        private const val PRICE_FLOAT = 1.2511f
        private val PRICE = BigDecimal(PRICE_FLOAT.toDouble())
        private val CURRENCY = PriceCurrency.getInstance("USD")
        private val EXCHANGE_RATE = ExchangeRateBuilderFactory().setBaseCurrency(CURRENCY).build()
    }

    private lateinit var price: ImmutablePriceImpl
    private lateinit var priceWith3DigitsOfPrecision: ImmutablePriceImpl

    @Before
    fun setUp() {
        price = ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE)
        priceWith3DigitsOfPrecision = ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE, 3)
    }

    @Test
    @Throws(Exception::class)
    fun getPriceAsFloat() {
        assertEquals(PRICE_FLOAT, price.priceAsFloat, TestUtils.EPSILON)
        assertEquals(PRICE_FLOAT, priceWith3DigitsOfPrecision.priceAsFloat, TestUtils.EPSILON)
    }

    @Test
    @Throws(Exception::class)
    fun getPrice() {
        assertEquals(PRICE.toDouble(), price.price.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(
            PRICE.toDouble(),
            priceWith3DigitsOfPrecision.price.toDouble(),
            TestUtils.EPSILON.toDouble()
        )
    }

    @Test
    @Throws(Exception::class)
    fun getDecimalFormattedPrice() {
        assertEquals("1.25", price.decimalFormattedPrice)
        assertEquals("1.251", priceWith3DigitsOfPrecision.decimalFormattedPrice)
    }

    @Test
    @Throws(Exception::class)
    fun getCurrencyFormattedPrice() {
        assertEquals("$1.25", price.currencyFormattedPrice)
        assertEquals("$1.251", priceWith3DigitsOfPrecision.currencyFormattedPrice)
    }

    @Test
    @Throws(Exception::class)
    fun getCurrencyCodeFormattedPrice() {
        assertEquals("USD1.25", price.currencyCodeFormattedPrice)
        assertEquals("USD1.251", priceWith3DigitsOfPrecision.currencyCodeFormattedPrice)
    }

    @Test
    @Throws(Exception::class)
    fun getCurrency() {
        assertEquals(CURRENCY, price.currency)
        assertEquals(CURRENCY, priceWith3DigitsOfPrecision.currency)
    }

    @Test
    @Throws(Exception::class)
    fun getCurrencyCode() {
        assertEquals("USD", price.currencyCode)
        assertEquals("USD", priceWith3DigitsOfPrecision.currencyCode)
    }

    @Test
    @Throws(Exception::class)
    fun getExchangeRate() {
        assertEquals(EXCHANGE_RATE, price.exchangeRate)
        assertEquals(EXCHANGE_RATE, priceWith3DigitsOfPrecision.exchangeRate)
    }

    @Test
    fun testToString() {
        assertEquals("$1.25", price.toString())
        assertEquals("$1.251", priceWith3DigitsOfPrecision.toString())
    }

    @Test
    @Throws(Exception::class)
    fun parcel() {
        val parcel = Parcel.obtain()
        price.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val parcelPrice = ImmutablePriceImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(parcelPrice)
        assertEquals(price, parcelPrice)
    }

    @Test
    fun equals() {
        Assert.assertEquals(price, price)
        Assert.assertEquals(price, ImmutablePriceImpl(PRICE, CURRENCY, EXCHANGE_RATE))
        assertThat(price, not(equalTo(Any())))
        assertThat<Parcelable>(
            price,
            not<Parcelable>(equalTo<Parcelable>(mock(Distance::class.java)))
        )
        assertThat(price, not(equalTo(ImmutablePriceImpl(BigDecimal(0), CURRENCY, EXCHANGE_RATE))))
        assertThat(
            price,
            not(equalTo(ImmutablePriceImpl(PRICE, PriceCurrency.getInstance("EUR"), EXCHANGE_RATE)))
        )
    }
}