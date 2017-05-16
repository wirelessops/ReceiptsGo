package co.smartreceipts.android.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

public class UriUtils {

    private UriUtils() {

    }

    @Nullable
    public static String getExtension(@NonNull File file, @NonNull Context context) {
        return getExtension(Uri.fromFile(file), context);
    }

    @Nullable
    public static String getExtension(@NonNull Uri uri, @NonNull Context context) {
        return getExtension(uri, context.getContentResolver());
    }

    @Nullable
    public static String getExtension(@NonNull Uri uri, @NonNull ContentResolver contentResolver) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) { // scheme is content://
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
        } else { // scheme is file://
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
    }

    @NonNull
    public static String getMimeType(@NonNull File file, @NonNull Context context) {
        return getMimeType(Uri.fromFile(file), context);
    }

    @NonNull
    public static String getMimeType(@NonNull Uri uri, @NonNull Context context) {
        return getMimeType(uri, context.getContentResolver());
    }

    @NonNull
    public static String getMimeType(@NonNull Uri uri, @NonNull ContentResolver contentResolver) {
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(uri, contentResolver));
        return mimeType != null ? mimeType : "";
    }
}
