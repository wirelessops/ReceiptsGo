package com.wops.receiptsgo.workers.reports.pdf.fonts;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class LocalAssetPdfFontLoader implements PdfFontLoader {

    private final Context context;
    private final PDDocument document;
    private final Map<String, PDFont> fontCache = new HashMap<>();

    public LocalAssetPdfFontLoader(@NonNull Context context, @NonNull PDDocument document) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.document = Preconditions.checkNotNull(document);
    }

    @NonNull
    @Override
    public PDFont load(@NonNull String fontFile) throws IOException {
        PDFont pdFont = fontCache.get(fontFile);
        if (pdFont == null) {
            pdFont = PDType0Font.load(document, context.getAssets().open(fontFile));
            fontCache.put(fontFile, pdFont);
        }
        return pdFont;
    }
}
