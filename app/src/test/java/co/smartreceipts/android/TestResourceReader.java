package co.smartreceipts.android;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;

public class TestResourceReader {

    public static final String RECEIPT_JPG = "receipt.jpg";
    public static final String RECEIPT_PNG = "receipt.png";
    public static final String RECEIPT_PDF = "receipt.pdf";
    public static final String LONG_RECEIPT_JPG = "long_receipt.jpg";
    public static final String WIDE_RECEIPT_JPG = "wide_receipt.jpg";
    public static final String TWO_PAGE_RECEIPT_PDF = "two_page_receipt.pdf";

    @NonNull
    public File openFile(@NonNull String resourceName) {
        return new File(getClass().getClassLoader().getResource(resourceName).getFile());
    }

    @NonNull
    public InputStream openStream(@NonNull String resourceName) {
        return getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}
