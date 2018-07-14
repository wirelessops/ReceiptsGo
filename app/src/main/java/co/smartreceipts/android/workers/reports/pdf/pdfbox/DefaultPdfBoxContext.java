package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import co.smartreceipts.android.R;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorManager;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontManager;

public class DefaultPdfBoxContext implements PdfBoxContext {

    private final Context localizedContext;
    private final PdfFontManager fontManager;
    private final PdfColorManager colorManager;
    private final UserPreferenceManager preferences;

    private PDRectangle pageSize;

    public DefaultPdfBoxContext(@NonNull Context localizedContext,
                                @NonNull PdfFontManager fontManager,
                                @NonNull PdfColorManager colorManager,
                                @NonNull UserPreferenceManager preferences) {
        this.localizedContext = Preconditions.checkNotNull(localizedContext);
        this.fontManager = Preconditions.checkNotNull(fontManager);
        this.colorManager = Preconditions.checkNotNull(colorManager);
        this.preferences = Preconditions.checkNotNull(preferences);

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
    public PdfFontManager getFontManager() {
        return fontManager;
    }

    @NonNull
    @Override
    public PdfColorManager getColorManager() {
        return colorManager;
    }

}
