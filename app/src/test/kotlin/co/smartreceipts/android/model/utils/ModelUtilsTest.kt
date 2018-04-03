package co.smartreceipts.android.model.utils

import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.utils.TestLocaleToggler
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class ModelUtilsTest {

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getDecimalFormattedValueForFloat() {
        assertEquals("2.21", ModelUtils.getDecimalFormattedValue(2.21f))
    }

    @Test
    fun getDecimalFormattedValueForBigDecimal() {
        assertEquals("2.54", ModelUtils.getDecimalFormattedValue(BigDecimal(2.54)))
    }

    @Test
    fun getDecimalFormattedValueWithPrecision() {
        assertEquals("2.541", ModelUtils.getDecimalFormattedValue(BigDecimal(2.5412), 3))
        assertEquals("2.5", ModelUtils.getDecimalFormattedValue(BigDecimal(2.5412), 1))
    }

    @Test
    fun getCurrencyFormattedValue() {
        assertEquals(
            "$2.54",
            ModelUtils.getCurrencyFormattedValue(BigDecimal(2.54), PriceCurrency.getInstance("USD"))
        )
        assertEquals("2.54", ModelUtils.getCurrencyFormattedValue(BigDecimal(2.54), null))
    }

    @Test
    fun getCurrencyCodeFormattedValue() {
        assertEquals(
            "USD2.54",
            ModelUtils.getCurrencyCodeFormattedValue(
                BigDecimal(2.54),
                PriceCurrency.getInstance("USD")
            )
        )
        assertEquals("2.54", ModelUtils.getCurrencyCodeFormattedValue(BigDecimal(2.54), null))
    }

    @Test
    fun tryParse() {
        assertEquals(ModelUtils.tryParse(null), BigDecimal(0))
        assertEquals(ModelUtils.tryParse(null, BigDecimal(1)), BigDecimal(1))
    }

    @Test
    fun isPriceZero() {
        assertTrue(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(0.0).setCurrency("USD").build()))
        assertFalse(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(0.1).setCurrency("USD").build()))
        assertFalse(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(-0.1).setCurrency("USD").build()))
        assertFalse(
            ModelUtils.isPriceZero(
                PriceBuilderFactory().setPrice(java.lang.Float.MAX_VALUE.toDouble()).setCurrency(
                    "USD"
                ).build()
            )
        )
        assertFalse(
            ModelUtils.isPriceZero(
                PriceBuilderFactory().setPrice((-java.lang.Float.MAX_VALUE).toDouble()).setCurrency(
                    "USD"
                ).build()
            )
        )
    }

}