package co.smartreceipts.android.model

import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.TestUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.*

@RunWith(RobolectricTestRunner::class)
class TaxItemTest {

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun constructionTest() {
        val floatPercent = 20.2f
        val stringPercent = "20.20"
        val bigFloatPercent = BigDecimal(floatPercent.toDouble())
        val bigStringPercent = BigDecimal(stringPercent)
        val t1 = TaxItem(floatPercent, true)
        val t2 = TaxItem(stringPercent, true)
        val t3 = TaxItem(bigFloatPercent, true)
        val t4 = TaxItem(bigStringPercent, true)
        val t5 = TaxItem(floatPercent, false)
        val t6 = TaxItem(stringPercent, false)
        val t7 = TaxItem(bigFloatPercent, false)
        val t8 = TaxItem(bigStringPercent, false)
        assertEquals(t2.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t2.percentAsString, t1.percentAsString)
        assertEquals(t3.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t3.percentAsString, t1.percentAsString)
        assertEquals(t4.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t4.percentAsString, t1.percentAsString)
        assertEquals(t5.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t5.percentAsString, t1.percentAsString)
        assertEquals(t6.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t6.percentAsString, t1.percentAsString)
        assertEquals(t7.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t7.percentAsString, t1.percentAsString)
        assertEquals(t8.percent.toDouble(), t1.percent.toDouble(), TestUtils.EPSILON.toDouble())
        assertEquals(t8.percentAsString, t1.percentAsString)
    }


    @Test
    fun setPriceTest() {
        /* Post-Tax Formula
		 x = total, y = price, t = x - y, t = y * p
		 x = y + y * p
		 x = y * (1 + p)
		 y = x / (1 + p)
		 t = x - y
		 t = x - x / (1 + p)
		 t = x ( 1 - 1 / (1 + p) )
		 */
        val price = "100.00"
        val percent1 = 20.25f
        val percent2 = 50.43f
        val preTax1 = TaxItem(percent1, true)
        val postTax1 = TaxItem(percent1, false)
        val preTax2 = TaxItem(percent2, true)
        val postTax2 = TaxItem(percent2, false)
        preTax1.setPrice(price)
        postTax1.setPrice(price)
        preTax2.setPrice(price)
        postTax2.setPrice(price)
        assertEquals(preTax1.tax!!.toDouble(), 20.25, TestUtils.EPSILON.toDouble())
        assertEquals(preTax1.toString(), "20.25")
        assertEquals(postTax1.tax!!.toDouble(), 16.84, TestUtils.EPSILON.toDouble())
        assertEquals(postTax1.toString(), "16.84")
        assertEquals(preTax2.tax!!.toDouble(), 50.43, TestUtils.EPSILON.toDouble())
        assertEquals(preTax2.toString(), "50.43")
        assertEquals(postTax2.tax!!.toDouble(), 33.52, TestUtils.EPSILON.toDouble())
        assertEquals(postTax2.toString(), "33.52")
    }

    @Test
    fun setPriceDifferentLocaleSeparatorTest() {
        val priceDot = "5.0"
        val priceComma = "5,0"

        val percent = 100.0f

        val taxDot = TaxItem(percent, true)
        taxDot.setPrice(priceDot)

        TestLocaleToggler.setDefaultLocale(Locale.GERMANY)
        val taxComma = TaxItem(percent, true)
        taxComma.setPrice(priceComma)

        assertEquals(0,  taxComma.tax.compareTo(taxDot.tax))
    }
}
