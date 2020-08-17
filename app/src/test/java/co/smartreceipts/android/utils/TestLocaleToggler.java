package co.smartreceipts.android.utils;

import androidx.annotation.NonNull;

import java.util.Locale;

public class TestLocaleToggler {

    private static Locale originalLocale;

    public static void setDefaultLocale(@NonNull Locale locale) {
        originalLocale = Locale.getDefault();
        Locale.setDefault(locale);
    }

    public static void resetDefaultLocale() {
        if (originalLocale == null) {
            throw new IllegalArgumentException("Cannot reset the default Locale without calling the setter method.");
        }
        Locale.setDefault(originalLocale);
        originalLocale = null;
    }
}
