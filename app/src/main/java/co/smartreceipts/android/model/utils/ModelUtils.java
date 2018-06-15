package co.smartreceipts.android.model.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;

/**
 * A utility class, which will be used to standard some common functions that are
 * shared across multiple model objects
 */
public class ModelUtils {

    private static final Map<Integer, DecimalFormat> sDecimalFormatCache = new ConcurrentHashMap<>();
    private static final float EPSILON = 0.0001f;

    private ModelUtils() {
        throw new RuntimeException("This class uses static calls only. It cannot be instantiated");
    }

    public static String getFormattedDate(@NonNull java.util.Date date, @NonNull TimeZone timeZone, @NonNull Context context, @NonNull String separator) {
        return getFormattedDate(new Date(date.getTime()), timeZone, context, separator);
    }

    /**
     * Gets a formatted version of a date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param date      - the {@link Date} to format
     * @param timeZone  - the {@link TimeZone} to use for this date
     * @param context   - the current {@link Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the start date
     */
    public static String getFormattedDate(@NonNull Date date, @NonNull TimeZone timeZone, @NonNull Context context, @NonNull String separator) {
        final java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone); // Hack to shift the timezone appropriately
        final String formattedDate = format.format(date);
        return formattedDate.replace(DateUtils.getDateSeparator(context), separator);
    }

    /**
     * Generates "decimal-formatted" value, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2001910"
     *
     * @param number - the {@link BigDecimal} to format
     * @return the decimal formatted price {@link String}
     */
    public static String getDecimalFormattedValue(float number) {
        return getDecimalFormattedValue(BigDecimal.valueOf(number));
    }

    /**
     * Generates "decimal-formatted" value, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2001910"
     *
     * @param decimal - the {@link BigDecimal} to format
     * @return the decimal formatted price {@link String}
     */
    @NonNull
    public static String getDecimalFormattedValue(@NonNull BigDecimal decimal) {
        return getDecimalFormattedValue(decimal, Price.DEFAULT_DECIMAL_PRECISION);
    }

    /**
     * Generates "decimal-formatted" value, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2001910". The number of decimal digits is based on the set precision
     *
     * @param decimal   - the {@link BigDecimal} to format
     * @param precision - the number of digits precision to use
     * @return the decimal formatted price {@link String}
     */
    @NonNull
    public static String getDecimalFormattedValue(@NonNull BigDecimal decimal, int precision) {
        // Note: I'm not concerned if we have a few duplicate entries (ie this isn't fully thread safe) as the objects are all equal
        DecimalFormat decimalFormat = sDecimalFormatCache.get(precision);
        if (decimalFormat == null) {
            decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(precision);
            decimalFormat.setMinimumFractionDigits(precision);
            decimalFormat.setGroupingUsed(false);
            sDecimalFormatCache.put(precision, decimalFormat);
        }
        return decimalFormat.format(decimal);
    }

    /**
     * The "currency-formatted" value, which would appear as "$25.20" or "$25,20" as determined by the user's locale.
     * By default, this assumes a decimal precision of {@link Price#DEFAULT_DECIMAL_PRECISION}
     *
     * @param decimal  - the {@link BigDecimal} to format
     * @param currency - the {@link PriceCurrency} to use. If this is {@code null}, return {@link #getDecimalFormattedValue(BigDecimal)}
     * @return - the currency formatted price {@link String}
     */
    public static String getCurrencyFormattedValue(@NonNull BigDecimal decimal, @Nullable PriceCurrency currency) {
        return getCurrencyFormattedValue(decimal, currency, Price.DEFAULT_DECIMAL_PRECISION);
    }

    /**
     * The "currency-formatted" value, which would appear as "$25.20" or "$25,20" as determined by the user's locale.
     *
     * @param decimal  - the {@link BigDecimal} to format
     * @param currency - the {@link PriceCurrency} to use. If this is {@code null}, return {@link #getDecimalFormattedValue(BigDecimal)}
     * @param decimalPrecision - the desired decimal precision to use (eg 2 => "$25.20", 3 => "$25.200")
     * @return - the currency formatted price {@link String}
     */
    public static String getCurrencyFormattedValue(@NonNull BigDecimal decimal, @Nullable PriceCurrency currency,
                                                   int decimalPrecision) {
        if (currency != null) {
            return currency.format(decimal, decimalPrecision);
        } else {
            return getDecimalFormattedValue(decimal, decimalPrecision);
        }
    }

    /**
     * The "currency-code-formatted" value, which would appear as "USD25.20" or "USD25,20" as determined by the user's locale.
     * By default, this assumes a decimal precision of {@link Price#DEFAULT_DECIMAL_PRECISION}
     *
     * @param decimal  - the {@link BigDecimal} to format
     * @param currency - the {@link PriceCurrency} to use. If this is {@code null}, return {@link #getDecimalFormattedValue(BigDecimal)}
     * @return - the currency formatted price {@link String}
     */
    public static String getCurrencyCodeFormattedValue(@NonNull BigDecimal decimal, @Nullable PriceCurrency currency) {
        return getCurrencyCodeFormattedValue(decimal, currency, Price.DEFAULT_DECIMAL_PRECISION);
    }

    /**
     * The "currency-code-formatted" value, which would appear as "USD25.20" or "USD25,20" as determined by the user's locale.
     *
     * @param decimal  - the {@link BigDecimal} to format
     * @param currency - the {@link PriceCurrency} to use. If this is {@code null}, return {@link #getDecimalFormattedValue(BigDecimal)}
     * @param decimalPrecision - the desired decimal precision to use (eg 2 => "USD25.20", 3 => "USD25.200")
     * @return - the currency formatted price {@link String}
     */
    public static String getCurrencyCodeFormattedValue(@NonNull BigDecimal decimal, @Nullable PriceCurrency currency,
                                                       int decimalPrecision) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (currency != null) {
            stringBuilder.append(currency.getCurrencyCode());
        }
        stringBuilder.append(getDecimalFormattedValue(decimal, decimalPrecision));
        return stringBuilder.toString();
    }

    /**
     * Tries to parse a string to find the underlying numerical value
     *
     * @param number the string containing a number (hopefully)
     * @return the {@link BigDecimal} value
     * @throws NumberFormatException if we cannot parse this number
     */
    @NonNull
    public static BigDecimal parseOrThrow(@Nullable String number) throws NumberFormatException {
        if (number == null || TextUtils.isEmpty(number)) {
            throw new NumberFormatException("Cannot parse an empty string");
        }
        return new BigDecimal(number.replace(",", "."));
    }

    /**
     * Tries to parse a string to find the underlying numerical value
     *
     * @param number the string containing a number (hopefully)
     * @return the {@link BigDecimal} value or "0" if it cannot be found
     */
    @NonNull
    public static BigDecimal tryParse(@Nullable String number) {
        return tryParse(number, BigDecimal.ZERO);
    }

    /**
     * Tries to parse a string to find the underlying numerical value
     *
     * @param number the string containing a number (hopefully)
     * @param defaultValue the default value to use if this string is not parseable
     * @return the {@link BigDecimal} value or "0" if it cannot be found
     */
    @NonNull
    public static BigDecimal tryParse(@Nullable String number, @NonNull BigDecimal defaultValue) {
        try {
            return ModelUtils.parseOrThrow(number);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Checks if a given price has a zero value
     *
     * @param price the {@link Price} to test
     * @return {@code true} if the price is equal to 0. {@code false} otherwise
     */
    public static boolean isPriceZero(@NonNull Price price) {
        return price.getPriceAsFloat() < EPSILON && price.getPriceAsFloat() > -1 * EPSILON;
    }

    @VisibleForTesting
    public static void clearStaticCachesForTesting() {
        sDecimalFormatCache.clear();
    }
}
