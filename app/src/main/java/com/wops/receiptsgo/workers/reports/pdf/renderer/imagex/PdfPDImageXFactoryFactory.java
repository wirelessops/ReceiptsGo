package com.wops.receiptsgo.workers.reports.pdf.renderer.imagex;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;

import com.wops.receiptsgo.utils.ConfigurableStaticFeature;

public class PdfPDImageXFactoryFactory {

    private final Context context;
    private final PDDocument pdDocument;
    private final File file;

    public PdfPDImageXFactoryFactory(@NonNull Context context, @NonNull PDDocument pdDocument, @NonNull File file) {
        this.context = Preconditions.checkNotNull(context);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.file = Preconditions.checkNotNull(file);
    }

    @NonNull
    public PdfPDImageXFactory get() {
        return new LollipopPdfPDImageXFactory(pdDocument, file);
    }
}
