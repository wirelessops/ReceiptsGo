package com.wops.receiptsgo.workers.reports.pdf.fonts;

import androidx.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

interface PdfFontLoader {

    /**
     * Attempts to load a font from a given font file name
     *
     * @param fontFile the font file name. Presumably a ttg
     * @return the loaded {@link PDFont}
     * @throws IOException if the load process failed
     */
    @NonNull
    PDFont load(@NonNull String fontFile) throws IOException;
}
