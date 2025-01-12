package com.wops.receiptsgo.workers.reports.pdf.pdfbox;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.workers.reports.pdf.colors.PdfColorManager;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontManager;

public class DefaultPdfBoxContext implements PdfBoxContext {

    private final Context localizedContext;
    private final PdfFontManager fontManager;
    private final PdfColorManager colorManager;
    private final UserPreferenceManager preferences;
    private final DateFormatter dateFormatter;

    private PDRectangle pageSize;

    public DefaultPdfBoxContext(@NonNull Context localizedContext,
                                @NonNull PdfFontManager fontManager,
                                @NonNull PdfColorManager colorManager,
                                @NonNull UserPreferenceManager preferences,
                                @NonNull DateFormatter dateFormatter) {
        this.localizedContext = Preconditions.checkNotNull(localizedContext);
        this.fontManager = Preconditions.checkNotNull(fontManager);
        this.colorManager = Preconditions.checkNotNull(colorManager);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);

        if (preferences.get(UserPreference.ReportOutput.DefaultPdfPageSize).equals(localizedContext.getString(R.string.pref_output_pdf_page_size_letter_entryValue))) {
            pageSize = PDRectangle.LETTER;
        } else {
            pageSize = PDRectangle.A4;
        }
    }

    @Override
    public float getPageMarginHorizontal() {
        return 32;
    }

    @Override
    public float getPageMarginVertical() {
        return 32;
    }

    @NonNull
    @Override
    public String getString(@StringRes int resId, Object... args) {
        return localizedContext.getString(resId, args);
    }

    @Override
    public void setPageSize(@NonNull PDRectangle rectangle) {
        pageSize = Preconditions.checkNotNull(rectangle);
    }

    @NonNull
    @Override
    public Context getAndroidContext() {
        return localizedContext;
    }

    @NonNull
    @Override
    public PDRectangle getPageSize() {
        return pageSize;
    }

    @NonNull
    @Override
    public UserPreferenceManager getPreferences() {
        return preferences;
    }

    @NonNull
    @Override
    public DateFormatter getDateFormatter() {
        return dateFormatter;
    }

    @NonNull
    @Override
    public PdfFontManager getFontManager() {
        return fontManager;
    }

    @NonNull
    @Override
    public PdfColorManager getColorManager() {
        return colorManager;
    }

}
