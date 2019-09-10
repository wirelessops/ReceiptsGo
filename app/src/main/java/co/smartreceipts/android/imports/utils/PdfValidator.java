package co.smartreceipts.android.imports.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.renderer.imagex.PdfPDImageXFactory;
import co.smartreceipts.android.workers.reports.pdf.renderer.imagex.PdfPDImageXFactoryFactory;

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
