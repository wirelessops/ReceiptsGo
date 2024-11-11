package com.wops.receiptsgo.workers.reports.pdf.pdfbox;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.workers.reports.pdf.colors.PdfColorManager;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontManager;

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
