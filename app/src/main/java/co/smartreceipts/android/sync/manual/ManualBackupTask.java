package co.smartreceipts.android.sync.manual;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.inject.Inject;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import wb.android.storage.StorageManager;

@ApplicationScope
public class ManualBackupTask {

    public static final String DATABASE_EXPORT_NAME = "receipts_backup.db";

    private static final String EXPORT_FILENAME = DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_SmartReceipts.smr";
    private static final String DATABASE_JOURNAL = "receipts.db-journal";

    private final Context context;
    private final SmartReceiptsTemporaryFileCache smartReceiptsTemporaryFileCache;
    private final PersistenceManager persistenceManager;
    private final Scheduler observeonscheduler;
    private final Scheduler subscribeOnScheduler;
    private ReplaySubject<File> backupBehaviorSubject;

    @Inject
    ManualBackupTask(@NonNull Context context, @NonNull PersistenceManager persistenceManager) {
        this(context, new SmartReceiptsTemporaryFileCache(context), persistenceManager, Schedulers.io(), Schedulers.io());
    }

    ManualBackupTask(@NonNull Context context,
                     @NonNull SmartReceiptsTemporaryFileCache smartReceiptsTemporaryFileCache,
                     @NonNull PersistenceManager persistenceManager,
                     @NonNull Scheduler observeOnScheduler,
                     @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.smartReceiptsTemporaryFileCache = Preconditions.checkNotNull(smartReceiptsTemporaryFileCache);
        this.persistenceManager = Preconditions.checkNotNull(persistenceManager);
        this.observeonscheduler = Preconditions.checkNotNull(observeOnScheduler);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @NonNull
    public synchronized ReplaySubject<File> backupData() {
        if (backupBehaviorSubject == null) {
            backupBehaviorSubject = ReplaySubject.create();
            backupDataToSingle()
                    .observeOn(observeonscheduler)
                    .subscribeOn(subscribeOnScheduler)
                    .toObservable()
                    .subscribe(backupBehaviorSubject);
        }
        return backupBehaviorSubject;
    }

    public synchronized void markBackupAsComplete() {
        backupBehaviorSubject = null;
    }

    @NonNull
    private Single<File> backupDataToSingle() {
        return Single.fromCallable(() -> {

            final StorageManager external = persistenceManager.getExternalStorageManager();
            final StorageManager internal = persistenceManager.getInternalStorageManager();
            external.delete(external.getFile(EXPORT_FILENAME)); // Remove old export

            external.copy(external.getFile(DatabaseHelper.DATABASE_NAME), external.getFile(DATABASE_EXPORT_NAME), true);
            final File internalSharedPreferencesFolder = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");

            // Preferences File
            if (internalSharedPreferencesFolder != null && internalSharedPreferencesFolder.exists()) {
                File backupSharedPreferencesFolder = external.getFile("shared_prefs");
                // Delete this first to clear out any old instances of it before copying over
                //noinspection ResultOfMethodCallIgnored
                backupSharedPreferencesFolder.delete();
                if (backupSharedPreferencesFolder.exists() || backupSharedPreferencesFolder.mkdir()) {
                    final File smartReceiptsPreferencesFile = new File(internalSharedPreferencesFolder, "SmartReceiptsPrefFile.xml");
                    final File backupSmartReceiptsPreferencesFile = new File(backupSharedPreferencesFolder, "SmartReceiptsPrefFile.xml");
                    Logger.debug(ManualBackupTask.this, "Copying the prefs file from: {} to {}", smartReceiptsPreferencesFile.getAbsolutePath(), backupSmartReceiptsPreferencesFile.getAbsolutePath());
                    if (external.copy(smartReceiptsPreferencesFile, backupSmartReceiptsPreferencesFile, true)) {
                        Logger.debug(ManualBackupTask.this, "Successfully copied our preferences files");
                    } else {
                        throw new IOException("Failed to copy our shared prefs");
                    }
                } else {
                    throw new IOException("Failed to create a folder for our shared prefs");
                }
            }

            // Finish
            File zip = external.zipBuffered(8192, new BackupFileFilter());
            File backupFile = smartReceiptsTemporaryFileCache.getExternalCacheFile(EXPORT_FILENAME);

            if (zip.renameTo(backupFile)) {
                return backupFile;
            } else {
                throw new IOException("Failed to rename the backup file to: " + backupFile.getAbsolutePath());
            }

        });
    }

    private static final class BackupFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return !file.getName().equalsIgnoreCase(DatabaseHelper.DATABASE_NAME) &&
                    !file.getName().equalsIgnoreCase(DATABASE_JOURNAL) &&
                    !file.getName().endsWith(".smr"); //Ignore previous backups
        }
    }
}
