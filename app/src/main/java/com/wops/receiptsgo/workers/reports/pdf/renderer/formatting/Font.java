package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontSpec;

public class Font extends AbstractFormatting<PdfFontSpec> {

    public Font(@NonNull PdfFontSpec fontSpec) {
        super(fontSpec, PdfFontSpec.class);
    }
}
