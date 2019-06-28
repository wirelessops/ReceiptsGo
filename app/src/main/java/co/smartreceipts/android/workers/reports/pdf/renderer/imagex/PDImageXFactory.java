package co.smartreceipts.android.workers.reports.pdf.renderer.imagex;

import androidx.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

public interface PDImageXFactory {

    /**
     * @return a {@link PDImageXObject} to be used
     *
     * @throws IOException if an error occurs
     */
    @NonNull
    PDImageXObject get() throws IOException;
}
