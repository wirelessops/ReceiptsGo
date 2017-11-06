package co.smartreceipts.android.currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.util.Locale;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.utils.TestLocaleToggler;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PriceCurrencyTest {

    @Before
    public void setUp() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.US);
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void testUSDCurrency() {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        final PriceCurrency currency = PriceCurrency.getInstance("USD");
        assertEquals("USD", currency.getCurrencyCode());
        assertEquals("$1.25", currency.format(new BigDecimal(1.25123), 2));
    }

    @Test
    public void testUSDCurrencyWith3PrecisionPoints() {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        final PriceCurrency currency = PriceCurrency.getInstance("USD");
        assertEquals("USD", currency.getCurrencyCode());
        assertEquals("$1.251", currency.format(new BigDecimal(1.25123), 3));
    }

    @Test
    public void testUnknownCurrency() {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        final PriceCurrency currency = PriceCurrency.getInstance("ZZZ");
        assertEquals("ZZZ", currency.getCurrencyCode());
        assertEquals("ZZZ1.25", currency.format(new BigDecimal(1.25123), 2));
    }

}