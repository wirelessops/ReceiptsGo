package com.wops.receiptsgo.imports.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.workers.reports.pdf.renderer.imagex.PdfPDImageXFactory;
import com.wops.receiptsgo.workers.reports.pdf.renderer.imagex.PdfPDImageXFactoryFactory;

public class PdfValidator {

    private final Context context;

    public PdfValidator(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
    }

    public boolean isPdfValid(@NonNull File file) {
        try (PdfPDImageXFactory factory = new PdfPDImageXFactoryFactory(context, new PDDocument(), file).get()) {
            factory.open();
            // document has at least one page == it's valid
            return factory.nextPage();
        } catch (IOException e) {
            Logger.error(this, "Invalid PDF File", e);
            return false;
        }
    }
}
