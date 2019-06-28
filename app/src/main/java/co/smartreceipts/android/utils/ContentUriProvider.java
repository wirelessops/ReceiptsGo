package co.smartreceipts.android.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.android.utils.log.Logger;

/** 
 * Some (ie just one) phone manufacturers (ie Huawei) break the Android contract for calls to {@link Context#getExternalFilesDirs(String)}. 
 * Rather than returning {@link Context#getExternalFilesDir(String)} (ie the default entry) as the first entry in the 
 * array returned by the former call, it always returns the external sd card if presentFirstTimeInformation. This class provides a reflection-based
 * hack to protect against Huawei device weirdness in these cases
 */
public class ContentUriProvider {

    private static final String HUAWEI_MANUFACTURER = "Huawei";

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            Logger.warn(ContentUriProvider.class, "Using a Huawei device Increased likelihood of failure...");
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Logger.warn(ContentUriProvider.class, "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug for pre-N devices", e);
                    return Uri.fromFile(file);
                } else {
                    Logger.warn(ContentUriProvider.class, "ANR Risk -- Copying the file the location cache to avoid Huawei 'external-files-path' bug for N+ devices", e);
                    final SmartReceiptsTemporaryFileCache temporaryFileCache = new SmartReceiptsTemporaryFileCache(context);
                    final File cacheLocation = temporaryFileCache.getInternalCacheFile(file.getName());
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new FileInputStream(file);
                        out = new FileOutputStream(cacheLocation); // appending output stream
                        IOUtils.copy(in, out);
                        Logger.info(ContentUriProvider.class, "Completed Android N+ Huawei file copy. Attempting to return the cached file");
                        return FileProvider.getUriForFile(context, authority, cacheLocation);
                    } catch (IOException e1) {
                        Logger.error(ContentUriProvider.class, "Failed to copy the Huawei file. Re-throwing exception", e1);
                        throw new IllegalArgumentException("Huawei devices are unsupported for Android N", e1);
                    } finally {
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }

}
