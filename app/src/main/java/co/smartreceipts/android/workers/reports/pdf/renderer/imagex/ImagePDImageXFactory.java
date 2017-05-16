package co.smartreceipts.android.workers.reports.pdf.renderer.imagex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import co.smartreceipts.android.utils.UriUtils;

public class ImagePDImageXFactory implements PDImageXFactory {

    private final Context context;
    private final PDDocument pdDocument;
    private final File file;

    public ImagePDImageXFactory(@NonNull Context context, @NonNull PDDocument pdDocument, @NonNull File file) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.file = Preconditions.checkNotNull(file);
    }

    @NonNull
    public PDImageXObject get() throws IOException {
        final String fileExtension = UriUtils.getExtension(file, context);
        Preconditions.checkNotNull(fileExtension, "This file does not have a valid extension: " + file);

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            if (fileExtension.toLowerCase().equals("jpg") || fileExtension.toLowerCase().equals("jpeg")) {
                return JPEGFactory.createFromStream(pdDocument, fileInputStream);
            } else if (fileExtension.toLowerCase().equals("png")) {
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(fileInputStream);
                    return LosslessFactory.createFromImage(pdDocument, bitmap);
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            } else {
                final String mimeType = UriUtils.getMimeType(file, context);
                if ("image/jpeg".equals(mimeType)) {
                    return JPEGFactory.createFromStream(pdDocument, fileInputStream);
                } else {
                    throw new IllegalArgumentException("Unknown file extension: " + fileExtension + ", with mime type: " + mimeType);
                }
            }
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }
}
