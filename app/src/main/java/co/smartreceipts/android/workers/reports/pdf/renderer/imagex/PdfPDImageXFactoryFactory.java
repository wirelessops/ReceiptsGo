package co.smartreceipts.android.workers.reports.pdf.renderer.imagex;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;

import co.smartreceipts.android.utils.ConfigurableStaticFeature;

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
        if (ConfigurableStaticFeature.CompatPdfRendering.isEnabled(context)) {
            return new CompatPdfPDImageXFactory(context, pdDocument, file);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new LollipopPdfPDImageXFactory(pdDocument, file);
            } else {
                return new CompatPdfPDImageXFactory(context, pdDocument, file);
            }
        }
    }
}
