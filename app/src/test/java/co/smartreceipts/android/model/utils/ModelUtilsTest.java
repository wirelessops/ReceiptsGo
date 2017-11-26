package co.smartreceipts.android.model.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.utils.TestLocaleToggler;

import static org.junit.Assert.*;

public class ModelUtilsTest {

    @Before
    public void setUp() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.US);
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void getDecimalFormattedValueForFloat() {
        assertEquals("2.21", ModelUtils.getDecimalFormattedValue(2.21f));
    }

    @Test
    public void getDecimalFormattedValueForBigDecimal() {
        assertEquals("2.54", ModelUtils.getDecimalFormattedValue(new BigDecimal(2.54)));
    }

    @Test
    public void getDecimalFormattedValueWithPrecision() {
        assertEquals("2.541", ModelUtils.getDecimalFormattedValue(new BigDecimal(2.5412), 3));
        assertEquals("2.5", ModelUtils.getDecimalFormattedValue(new BigDecimal(2.5412), 1));
    }

    @Test
    public void getCurrencyFormattedValue() {
        assertEquals("$2.54", ModelUtils.getCurrencyFormattedValue(new BigDecimal(2.54), PriceCurrency.getInstance("USD")));
        assertEquals("2.54", ModelUtils.getCurrencyFormattedValue(new BigDecimal(2.54), null));
    }

    @Test
    public void getCurrencyCodeFormattedValue() {
        assertEquals("USD2.54", ModelUtils.getCurrencyCodeFormattedValue(new BigDecimal(2.54), PriceCurrency.getInstance("USD")));
        assertEquals("2.54", ModelUtils.getCurrencyCodeFormattedValue(new BigDecimal(2.54), null));
    }

    @Test
    public void tryParse() {
        assertEquals(ModelUtils.tryParse(null), new BigDecimal(0));
        assertEquals(ModelUtils.tryParse(null, new BigDecimal(1)), new BigDecimal(1));
    }

    @Test
    public void isPriceZero() {
        assertTrue(ModelUtils.isPriceZero(new PriceBuilderFactory().setPrice(0).setCurrency("USD").build()));
        assertFalse(ModelUtils.isPriceZero(new PriceBuilderFactory().setPrice(0.1).setCurrency("USD").build()));
        assertFalse(ModelUtils.isPriceZero(new PriceBuilderFactory().setPrice(-0.1).setCurrency("USD").build()));
        assertFalse(ModelUtils.isPriceZero(new PriceBuilderFactory().setPrice(Float.MAX_VALUE).setCurrency("USD").build()));
        assertFalse(ModelUtils.isPriceZero(new PriceBuilderFactory().setPrice(-Float.MAX_VALUE).setCurrency("USD").build()));
    }

}