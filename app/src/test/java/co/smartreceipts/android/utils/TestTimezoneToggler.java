package co.smartreceipts.android.utils;

import androidx.annotation.NonNull;

import java.util.TimeZone;

public class TestTimezoneToggler {

    private static TimeZone originalTimeZone;

    public static void setDefaultTimeZone(@NonNull TimeZone timeZone) {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(timeZone);
    }

    public static void resetDefaultTimeZone() {
        if (originalTimeZone == null) {
            throw new IllegalArgumentException("Cannot reset the default TimeZone without calling the setter method.");
        }
        TimeZone.setDefault(originalTimeZone);
        originalTimeZone = null;
    }
}
