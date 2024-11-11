package com.wops.receiptsgo.workers.reports.pdf.renderer.imagex;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.utils.ImageUtils;

/**
 * Used to load pdf files via the native Android {@link PdfRenderer} stack
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopPdfPDImageXFactory implements PdfPDImageXFactory {

    private static final float IMAGE_QUALITY_SCALING_FACTOR = 2.75f;

    private final PDDocument pdDocument;
    private final File file;

    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private int currentPage = -1;

    public LollipopPdfPDImageXFactory(@NonNull PDDocument pdDocument, @NonNull File file) {
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.file = Preconditions.checkNotNull(file);
    }

    @Override
    public void open() throws IOException, SecurityException {
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    @NonNull
    @Override
    public PDImageXObject get() throws IOException {
        Preconditions.checkNotNull(pdfRenderer, "The Pdf file must first be opened");
        Preconditions.checkNotNull(parcelFileDescriptor, "The Pdf file must first be opened");

        PdfRenderer.Page page = null;
        Bitmap bitmap = null;
        try {
            Logger.debug(this, "Beginning the render of PDF page {} at {}", currentPage, System.currentTimeMillis());
            page = pdfRenderer.openPage(currentPage);
            final int scaledHeight = (int) IMAGE_QUALITY_SCALING_FACTOR * page.getHeight();
            final int scaledWidth = (int) IMAGE_QUALITY_SCALING_FACTOR * page.getWidth();
            final Rect destClip = new Rect(0, 0, scaledWidth, scaledHeight);
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
            page.render(bitmap, destClip, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            try {
                bitmap = ImageUtils.applyWhiteBackground(bitmap);
                bitmap = ImageUtils.changeCodec(bitmap, Bitmap.CompressFormat.JPEG, 100);
                Logger.debug(this, "Creating pdf image from converted JPEG to speed up processing time");
                return JPEGFactory.createFromImage(pdDocument, bitmap);
            } catch (IOException e) {
                // For some reason, the Lossless factory takes 15-20s per page whereas JPGs are vastly quicker
                Logger.warn(this, "Failed to convert to JPG to speed up our processing. Creating the bitmap from our lossless factory of PDF page {} at {}", currentPage, System.currentTimeMillis());
                return LosslessFactory.createFromImage(pdDocument, bitmap);
            }
        } finally {
            Logger.debug(this, "Completing the render of PDF page {} at {}", currentPage, System.currentTimeMillis());
            if (page != null) {
                page.close();
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Bumps us to use the next page
     *
     * @return {@code false} if we're on the last page. {@code true} otherwise
     */
    @Override
    public boolean nextPage() {
        return ++currentPage < pdfRenderer.getPageCount();
    }

    @Override
    public void close() throws IOException {
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        IOUtils.closeQuietly(parcelFileDescriptor);
    }
}
