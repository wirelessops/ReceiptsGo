package co.smartreceipts.android.model.utils

import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.utils.TestLocaleToggler
import org.joda.money.CurrencyUnit
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ModelUtilsTest {

    val currency: CurrencyUnit = CurrencyUnit.USD


    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
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
        assertEquals("$2.54", ModelUtils.getCurrencyFormattedValue(BigDecimal(2.54), currency))
        assertEquals("$2.5", ModelUtils.getCurrencyFormattedValue(BigDecimal(2.54), currency, 1))
        assertEquals("$2.540", ModelUtils.getCurrencyFormattedValue(BigDecimal(2.54), currency, 3))
    }

    @Test
    fun getCurrencyCodeFormattedValue() {
        assertEquals("USD 2.54", ModelUtils.getCurrencyCodeFormattedValue(BigDecimal(2.54), currency))
        assertEquals("USD 2.5", ModelUtils.getCurrencyCodeFormattedValue(BigDecimal(2.54), currency, 1))
        assertEquals("USD 2.540", ModelUtils.getCurrencyCodeFormattedValue(BigDecimal(2.54), currency, 3))
    }

    @Test
    fun tryParse() {
        assertEquals(BigDecimal(0), ModelUtils.tryParse(null))
        assertEquals(BigDecimal(1), ModelUtils.tryParse(null, BigDecimal(1)))

        TestLocaleToggler.setDefaultLocale(Locale.US)
        assertTrue(ModelUtils.tryParse("2.00").compareTo(BigDecimal("2.00")) == 0)
        assertTrue(ModelUtils.tryParse("1,050,555.256").compareTo(BigDecimal("1050555.256")) == 0)
        assertTrue(ModelUtils.tryParse("1050555.256").compareTo(BigDecimal("1050555.256")) == 0)

        TestLocaleToggler.setDefaultLocale(Locale.FRANCE)
        assertTrue(ModelUtils.tryParse("2,00").compareTo(BigDecimal("2.00")) == 0)
        assertTrue(ModelUtils.tryParse("1 050 555,256").compareTo(BigDecimal("1050555.256")) == 0)
        assertTrue(ModelUtils.tryParse("105 0555,256").compareTo(BigDecimal("1050555.256")) == 0)
    }

    @Test
    fun isPriceZero() {
        assertTrue(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(0.0).setCurrency("USD").build()))
        assertFalse(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(0.1).setCurrency("USD").build()))
        assertFalse(ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(-0.1).setCurrency("USD").build()))
        assertFalse(
            ModelUtils.isPriceZero(PriceBuilderFactory().setPrice(java.lang.Float.MAX_VALUE.toDouble()).setCurrency("USD").build())
        )
        assertFalse(
            ModelUtils.isPriceZero(PriceBuilderFactory().setPrice((-java.lang.Float.MAX_VALUE).toDouble()).setCurrency("USD").build())
        )
    }

    @Test
    fun differentLocaleFormatting() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        val us = ModelUtils.getCurrencyFormattedValue(BigDecimal(1000555.567), currency)

        TestLocaleToggler.setDefaultLocale(Locale.FRANCE)
        val fr = ModelUtils.getCurrencyFormattedValue(BigDecimal(1000555.567), CurrencyUnit.USD)

        assertNotEquals(us, fr)
        assertEquals("$1,000,555.57", us)
        assertEquals("USD1 000 555,57", fr)
    }

}