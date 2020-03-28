package co.smartreceipts.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
            final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
            if (extension == null) {
                return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
            } else {
                return extension;
            }
        } else { // scheme is file://
            final String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
            if (!TextUtils.isEmpty(extension)) {
                return extension;
            } else {
                // Sometimes there are issues with special characters (eg '!') for MimeType processing
                final int extensionIndex = uri.toString().lastIndexOf('.');
                if (extensionIndex < 0) {
                    return "";
                }
                else {
                    return uri.toString().substring(extensionIndex + 1);
                }
            }
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
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(uri, contentResolver));
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = contentResolver.getType(uri);
        }
        return mimeType != null ? mimeType : "";
    }
}
