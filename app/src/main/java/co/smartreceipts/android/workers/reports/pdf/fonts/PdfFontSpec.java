package co.smartreceipts.android.workers.reports.pdf.fonts;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;

public class PdfFontSpec {

    private final PDFont font;
    private final int size;

    public PdfFontSpec(@NonNull PDFont font, int size) {
        this.font = Preconditions.checkNotNull(font);
        this.size = size;
    }

    @NonNull
    public PDFont getFont() {
        return font;
    }

    public int getSize() {
            return size;
        }

}
