package co.smartreceipts.android.imports;

import java.util.Arrays;
import java.util.List;

public class RequestCodes {

    public static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    public static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;
    public static final int IMPORT_GALLERY_IMAGE = 5;
    public static final int IMPORT_GALLERY_PDF = 6;
    public static final int NATIVE_RETAKE_PHOTO_CAMERA_REQUEST = 8;
    public static final int ATTACH_GALLERY_IMAGE = 9;
    public static final int ATTACH_GALLERY_PDF = 10;

    public static final List<Integer> PHOTO_REQUESTS = Arrays.asList(
            NATIVE_NEW_RECEIPT_CAMERA_REQUEST,
            NATIVE_ADD_PHOTO_CAMERA_REQUEST,
            IMPORT_GALLERY_IMAGE,
            NATIVE_RETAKE_PHOTO_CAMERA_REQUEST,
            ATTACH_GALLERY_IMAGE);

    public static final List<Integer> PDF_REQUESTS = Arrays.asList(
            IMPORT_GALLERY_PDF,
            ATTACH_GALLERY_PDF);

    private RequestCodes() { }
}
