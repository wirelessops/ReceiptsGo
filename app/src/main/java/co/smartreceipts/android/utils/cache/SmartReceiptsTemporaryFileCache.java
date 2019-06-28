package co.smartreceipts.android.utils.cache;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;
import wb.android.storage.StorageManager;

/**
 * A bunch of classes.dex files also get saved in {@link Context#getCacheDir()}, so we uses this class
 * to create a special smart receipts subfolder that we can safely wipe upon each app launch
 */
@ApplicationScope
public class SmartReceiptsTemporaryFileCache {

    private static final String FOLDER_NAME = "smartReceiptsTmp";

    private final Context context;
    private final StorageManager storageManager;
    private final File internalTemporaryCacheFolder;
    private final File externalTemporaryCacheFolder;

    @Inject
    public SmartReceiptsTemporaryFileCache(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.storageManager = StorageManager.getInstance(context);
        this.internalTemporaryCacheFolder = new File(Preconditions.checkNotNull(context.getCacheDir()), FOLDER_NAME);
        this.externalTemporaryCacheFolder = Preconditions.checkNotNull(context.getExternalCacheDir());
    }

    /**
     * Returns a file in the <b>internal</b> cache folder
     *
     * @param filename the name of the file
     * @return the a {@link File}
     */
    @NonNull
    public File getInternalCacheFile(@NonNull String filename) {
        return new File(internalTemporaryCacheFolder, filename);
    }

    /**
     * Returns a file in the <b>external</b> cache folder
     *
     * @param filename the name of the file
     * @return the a {@link File}
     */
    @NonNull
    public File getExternalCacheFile(@NonNull String filename) {
        return new File(externalTemporaryCacheFolder, filename);
    }

    public void resetCache() {
        Logger.info(SmartReceiptsTemporaryFileCache.this, "Clearing the cached dir");
        Executors.newSingleThreadExecutor().execute(() -> {
            for (final File cacheDir : Arrays.asList(internalTemporaryCacheFolder, externalTemporaryCacheFolder)) {
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
                final File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        // Note: Only delete this file is it was modified more than a day ago to buy some cache buffer time
                        if (System.currentTimeMillis() > file.lastModified() + TimeUnit.DAYS.toMillis(1)) {
                            Logger.debug(SmartReceiptsTemporaryFileCache.this, "Recursively deleting cached file: {}", file);
                            storageManager.deleteRecursively(file);
                        }
                    }
                }
            }
        });
    }
}
