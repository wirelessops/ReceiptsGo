package com.wops.receiptsgo.ocr.util;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.ocr.apis.model.OcrResponse;

/**
 * A simple utility class, which will allow us to handle all the {@code null} risks that we need to deal
 * with when processing {@link OcrResponse}.
 */
public class OcrResponseParser {

    /**
     * Tt can be a major pain to control the UI to move our date forward (eg let's say it guesses 11-May-2012
     * instead of 11-May-2017, then we have to move the calendar forward 5 years). As a result, we define this
     * minimum confidence threshold level to ensure we only process the date when this level is met
     */
    private static final double MINIMUM_DATE_CONFIDENCE = 0.4;

    private final OcrResponse ocrResponse;

    public OcrResponseParser(@Nullable OcrResponse ocrResponse) {
        this.ocrResponse = ocrResponse;
    }

    /**
     * @return the value of {@link OcrResponse#getMerchant()} as a {@link String} or {@code null} if it was not found
     */
    @Nullable
    public String getMerchant() {
        if (ocrResponse != null && ocrResponse.getMerchant() != null && ocrResponse.getMerchant().getData() != null) {
            return ocrResponse.getMerchant().getData();
        } else {
            return null;
        }
    }

    /**
     * @return the value of {@link OcrResponse#getTotalAmount()} as a price {@link String} or {@code null} if it was not found
     */
    @Nullable
    public String getTotalAmount() {
        if (ocrResponse != null && ocrResponse.getTotalAmount() != null && ocrResponse.getTotalAmount().getData() != null) {
            return BigDecimal.valueOf(ocrResponse.getTotalAmount().getData()).
                    setScale(Price.TOTAL_DECIMAL_PRECISION, RoundingMode.HALF_EVEN).toPlainString();
        } else {
            return null;
        }
    }

    /**
     * @return the value of {@link OcrResponse#getTaxAmount()} as a price {@link String} or {@code null} if it was not found
     */
    @Nullable
    public String getTaxAmount() {
        if (ocrResponse != null && ocrResponse.getTaxAmount() != null && ocrResponse.getTaxAmount().getData() != null) {
            return BigDecimal.valueOf(ocrResponse.getTaxAmount().getData()).toPlainString();
        } else {
            return null;
        }
    }

    /**
     * @return the value of {@link OcrResponse#getDate()} as a price {@link Date} or {@code null} if it was not found (or invalid)
     */
    @Nullable
    public Date getDate() {
        if (ocrResponse != null && ocrResponse.getDate() != null && ocrResponse.getDate().getData() != null && ocrResponse.getDate().getConfidenceLevel() > MINIMUM_DATE_CONFIDENCE) {
            try {
                final SimpleDateFormat iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                final GregorianCalendar ocrResponseCalendar = new GregorianCalendar();
                ocrResponseCalendar.setTime(iso8601Formatter.parse(ocrResponse.getDate().getData()));
                ocrResponseCalendar.set(Calendar.HOUR_OF_DAY, 0);
                ocrResponseCalendar.set(Calendar.MINUTE, 0);
                ocrResponseCalendar.set(Calendar.SECOND, 0);
                ocrResponseCalendar.set(Calendar.MILLISECOND, 0);

                // Shift this by the current time (local) for hours/mins/secs/m
                final GregorianCalendar localTimeCalendar = new GregorianCalendar();
                localTimeCalendar.setTime(new Date(System.currentTimeMillis()));
                ocrResponseCalendar.add(Calendar.HOUR_OF_DAY, localTimeCalendar.get(Calendar.HOUR_OF_DAY));
                ocrResponseCalendar.add(Calendar.MINUTE, localTimeCalendar.get(Calendar.MINUTE));
                ocrResponseCalendar.add(Calendar.SECOND, localTimeCalendar.get(Calendar.SECOND));
                ocrResponseCalendar.add(Calendar.MILLISECOND, localTimeCalendar.get(Calendar.MILLISECOND));

                return new Date(ocrResponseCalendar.getTime().getTime());
            } catch (ParseException e) {
                Logger.error(this, "Failed to parse OCR Date.", e);
            }
        }
        return null;
    }
}
