package co.smartreceipts.android.ocr.util;

import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.model.OcrResponseField;
import co.smartreceipts.android.utils.TestLocaleToggler;
import co.smartreceipts.android.utils.TestTimezoneToggler;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OcrResponseParserTest {

    @Before
    public void setUp() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("America/New_York"));

    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
        TestTimezoneToggler.resetDefaultTimeZone();
    }

    @Test
    public void getMerchant() {
        assertNull(new OcrResponseParser(null).getMerchant());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, null, null, null, null, null)).getMerchant());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, null, new OcrResponseField<>(null, null), null, null, null)).getMerchant());
        assertEquals("merchant", new OcrResponseParser(new OcrResponse(null, null, null, null, new OcrResponseField<>("merchant", null), null, null, null)).getMerchant());
    }

    @Test
    public void getTotalAmount() {
        assertNull(new OcrResponseParser(null).getTotalAmount());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, null, null, null, null, null)).getTotalAmount());
        assertNull(new OcrResponseParser(new OcrResponse(new OcrResponseField<>(null, null), null, null, null, null, null, null, null)).getTotalAmount());
        assertEquals("23.30", new OcrResponseParser(new OcrResponse(new OcrResponseField<>(23.3, null), null, null, null, null, null, null, null)).getTotalAmount());
    }

    @Test
    public void getTaxAmount() {
        assertNull(new OcrResponseParser(null).getTaxAmount());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, null, null, null, null, null)).getTaxAmount());
        assertNull(new OcrResponseParser(new OcrResponse(null, new OcrResponseField<>(null, null), null, null, new OcrResponseField<>(null, null), null, null, null)).getTaxAmount());
        assertEquals("3.04", new OcrResponseParser(new OcrResponse(null, new OcrResponseField<>(3.04, null), null, null, null, null, null, null)).getTaxAmount());
    }

    @Test
    public void getDateAsIso8601String() {
        // Note: The before field is -360000000L, the after is +360000000L vs ET
        final Date date_2016_12_22_at_midnight = new Date(1482022800000L);
        final Date date_2016_12_23_at_midnight = new Date(1482829200000L);
        final String iso8601Date = "2016-12-22T12:00:00.000Z";
        Date ocrDate = null;

        assertNull(new OcrResponseParser(null).getDate());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, null, null, null, null, null)).getDate());
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(null, 1.0), null, null, null, null)).getDate());

        // Change timezone and test
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("America/New_York"));
        ocrDate = new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 1.0), null, null, null, null)).getDate();
        assertNotNull(ocrDate);
        assertTrue(ocrDate.after(date_2016_12_22_at_midnight));
        assertTrue(ocrDate.before(date_2016_12_23_at_midnight));
        assertTrue(getDateAsIso8601String(ocrDate).startsWith("2016-12-22"));

        // Change timezone and test
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("Europe/London"));
        ocrDate = new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 1.0), null, null, null, null)).getDate();
        assertNotNull(ocrDate);
        assertTrue(ocrDate.after(date_2016_12_22_at_midnight));
        assertTrue(ocrDate.before(date_2016_12_23_at_midnight));
        assertTrue(getDateAsIso8601String(ocrDate).startsWith("2016-12-22"));

        // Change timezone and test
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        ocrDate = new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 1.0), null, null, null, null)).getDate();
        assertNotNull(ocrDate);
        assertTrue(ocrDate.after(date_2016_12_22_at_midnight));
        assertTrue(ocrDate.before(date_2016_12_23_at_midnight));
        assertTrue(getDateAsIso8601String(ocrDate).startsWith("2016-12-22"));

        // Change timezone and test
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("Asia/Istanbul"));
        ocrDate = new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 1.0), null, null, null, null)).getDate();
        assertNotNull(ocrDate);
        assertTrue(ocrDate.after(date_2016_12_22_at_midnight));
        assertTrue(ocrDate.before(date_2016_12_23_at_midnight));
        assertTrue(getDateAsIso8601String(ocrDate).startsWith("2016-12-22"));
    }

    @Test
    public void getDateForBadString() {
        final String iso8601Date = "badDate";
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 1.0), null, null, null, null)).getDate());
    }

    @Test
    public void getDateWithLowConfidence() {
        final String iso8601Date = "2016-12-22T12:00:00.000Z";
        assertNull(new OcrResponseParser(new OcrResponse(null, null, null, new OcrResponseField<>(iso8601Date, 0.4), null, null, null, null)).getDate());
    }

    @NonNull
    private String getDateAsIso8601String(@NonNull Date date) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        return formatter.format(date);
    }

}