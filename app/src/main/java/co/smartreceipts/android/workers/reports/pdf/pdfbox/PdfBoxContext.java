package co.smartreceipts.android.workers.reports.pdf.pdfbox;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorManager;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontManager;

public interface PdfBoxContext {

    /**
     * The android application {@link Context}.
     * Used for formatting dates and providing String resources through the
     * {@link #getString(int, Object...)} method.
     *
     * @return
     */
    @NonNull
    Context getAndroidContext();

    /**
     * A {@link PDRectangle} that represents the full page size, eg A4 etc.
     * @return
     */
    @NonNull
    PDRectangle getPageSize();

    @NonNull
    UserPreferenceManager getPreferences();

    @NonNull
    DateFormatter getDateFormatter();

    @NonNull
    PdfFontManager getFontManager();

    @NonNull
    PdfColorManager getColorManager();

    float getPageMarginHorizontal();

    float getPageMarginVertical();

    @NonNull
    String getString(@StringRes int resId, Object... args);

    void setPageSize(@NonNull PDRectangle rectangle);

}
