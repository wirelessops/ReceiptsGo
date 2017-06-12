package co.smartreceipts.android.utils.cache;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.utils.log.Logger;
import wb.android.storage.StorageManager;

/**
 * A bunch of classes.dex files also get saved in {@link Context#getCacheDir()}, so we uses this class
 * to create a special smart receipts subfolder that we can safely wipe upon each app launch
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SmartReceiptsTemporaryFileCache {

    private static final String FOLDER_NAME = "smartReceiptsTmp";

    @Inject
    StorageManager storageManager;

    private final Context mContext;
    private final File mTemporaryCacheFolder;

    public SmartReceiptsTemporaryFileCache(@NonNull Context context) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mTemporaryCacheFolder = new File(Preconditions.checkNotNull(context.getCacheDir()), FOLDER_NAME);
    }

    @NonNull
    public File getFile(@NonNull String filename) {
        return new File(mTemporaryCacheFolder, filename);
    }

    public void resetCache() {
        Logger.info(SmartReceiptsTemporaryFileCache.this, "Clearing the cached dir");
        Executors.newSingleThreadExecutor().execute(() -> {
            mTemporaryCacheFolder.mkdirs();
            final File[] files = mTemporaryCacheFolder.listFiles();
            if (files != null) {
                final StorageManager storageManager1 = StorageManager.getInstance(mContext);
                for (final File file : files) {
                    // Note: Only delete this file is it was modified more than a day ago to buy some cache buffer time
                    if (System.currentTimeMillis() > file.lastModified() + TimeUnit.DAYS.toMillis(1)) {
                        Logger.debug(SmartReceiptsTemporaryFileCache.this, "Recursively deleting cached file: {}", file);
                        storageManager1.deleteRecursively(file);
                    }
                }
            }
        });
    }
}
