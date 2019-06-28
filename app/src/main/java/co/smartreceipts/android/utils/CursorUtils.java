package co.smartreceipts.android.utils;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;

/**
 * A series of static utilities for {@link Cursor} interactions
 */
public class CursorUtils {

    private CursorUtils() {
        throw new RuntimeException("Don't construct me");
    }
    
    public static boolean getBoolean(Cursor cursor, int columnIndex, boolean defaultValue) {
        if (columnIndex >= 0) {
            return (cursor.getInt(columnIndex) > 0);
        } else {
            return defaultValue;
        }
    }

    public static int getInt(Cursor cursor, int columnIndex, int defaultValue) {
        if (columnIndex >= 0) {
            return cursor.getInt(columnIndex);
        } else {
            return defaultValue;
        }
    }

    public static long getLong(Cursor cursor, int columnIndex, long defaultValue) {
        if (columnIndex >= 0) {
            return cursor.getLong(columnIndex);
        } else {
            return defaultValue;
        }
    }

    public static double getDouble(Cursor cursor, int columnIndex, double defaultValue) {
        if (columnIndex >= 0) {
            return cursor.getDouble(columnIndex);
        } else {
            return defaultValue;
        }
    }

    public static String getString(Cursor cursor, int columnIndex, String defaultValue) {
        if (columnIndex >= 0) {
            return cursor.getString(columnIndex);
        } else {
            return defaultValue;
        }
    }

    /**
     * Please note that a very frustrating bug exists here. Android cursors only return the first 6
     * characters of a price string if that string contains a '.' character. It returns all of them
     * if not. This means we'll break for prices over 5 digits unless we are using a comma separator,
     * which we'd do in the EU. In the EU (comma separated), Android returns the wrong value when we
     * get a double (instead of a string). This method has been built to handle this edge case to the
     * best of our abilities.
     * <p/>
     * TODO: Longer term, everything should be saved with a decimal point
     *
     * @param cursor - the current {@link Cursor}
     * @param columnIndex  - the columnIndex of the column
     * @return a {@link BigDecimal} value of the decimal
     * @see "https://code.google.com/p/android/issues/detail?id=22219."
     */
    public static BigDecimal getDecimal(@NonNull Cursor cursor, int columnIndex) {
        return getDecimal(cursor, columnIndex, BigDecimal.ZERO);
    }

    public static BigDecimal getDecimal(@NonNull Cursor cursor, int columnIndex, @Nullable BigDecimal defaultValue) {
        if (columnIndex >= 0) {
            final String decimalString = cursor.getString(columnIndex);
            final double decimalDouble = cursor.getDouble(columnIndex);
            if (!TextUtils.isEmpty(decimalString) && decimalString.contains(",")) {
                try {
                    return new BigDecimal(decimalString.replace(",", "."));
                } catch (NumberFormatException e) {
                    return BigDecimal.valueOf(decimalDouble);
                }
            } else {
                return BigDecimal.valueOf(decimalDouble);
            }
        } else {
            return defaultValue;
        }
    }
}
