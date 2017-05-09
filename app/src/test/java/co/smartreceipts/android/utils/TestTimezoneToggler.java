package co.smartreceipts.android.utils;

import android.support.annotation.NonNull;

import java.util.TimeZone;

import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.utils.ModelUtils;

public class TestTimezoneToggler {

    private static TimeZone originalTimeZone;

    public static void setDefaulTimeZone(@NonNull TimeZone timeZone) {
        PriceCurrency.clearStaticCachesForTesting();
        ModelUtils.clearStaticCachesForTesting();
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
