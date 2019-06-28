package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;

public class Font extends AbstractFormatting<PdfFontSpec> {

    public Font(@NonNull PdfFontSpec fontSpec) {
        super(fontSpec, PdfFontSpec.class);
    }
}
