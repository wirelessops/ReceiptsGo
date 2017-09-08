package co.smartreceipts.android.sync.manual;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.concurrent.Callable;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import wb.android.storage.StorageManager;

public class ManualBackupTask {

    public static final String DATABASE_EXPORT_NAME = "receipts_backup.db";

    private static final String EXPORT_FILENAME = DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_SmartReceipts.smr";
    private static final String DATABASE_JOURNAL = "receipts.db-journal";

    private final Context context;
    private final PersistenceManager persistenceManager;
    private final Scheduler observeonscheduler;
    private final Scheduler subscribeOnScheduler;
    private ReplaySubject<File> backupBehaviorSubject;

    ManualBackupTask(@NonNull Context context, @NonNull PersistenceManager persistenceManager) {
        this(context, persistenceManager, Schedulers.io(), Schedulers.io());
    }

    ManualBackupTask(@NonNull Context context, @NonNull PersistenceManager persistenceManager, @NonNull Scheduler observeOnScheduler, @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
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

    @NonNull
    private Single<File> backupDataToSingle() {
        return Single.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                final SmartReceiptsTemporaryFileCache smartReceiptsTemporaryFileCache = new SmartReceiptsTemporaryFileCache(context);
                final File outputSmrBackupFile = smartReceiptsTemporaryFileCache.getFile(EXPORT_FILENAME);
                final StorageManager external = persistenceManager.getExternalStorageManager();
                final StorageManager internal = persistenceManager.getInternalStorageManager();
                external.delete(external.getFile(EXPORT_FILENAME)); //Remove old export
                internal.delete(outputSmrBackupFile); //Remove old export

                external.copy(external.getFile(DatabaseHelper.DATABASE_NAME), external.getFile(DATABASE_EXPORT_NAME), true);
                final File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");

                //Preferences File
                if (prefs != null && prefs.exists()) {
                    File sdPrefs = external.getFile("shared_prefs");
                    Logger.debug(ManualBackupTask.this, "Copying the prefs file from: {} to {}", prefs.getAbsolutePath(), sdPrefs.getAbsolutePath());
                    try {
                        external.copy(prefs, sdPrefs, true);
                    } catch (IOException e) {
                        Logger.error(this, e);
                    }
                }

                //Internal Files
                final File[] internalFiles = internal.listFilesAndDirectories();
                if (internalFiles != null && internalFiles.length > 0) {
                    Logger.debug(ManualBackupTask.this, "Copying {} files/directories to the SD Card.", internalFiles.length);
                    final File internalOnSD = external.mkdir("Internal");
                    internal.copy(internalOnSD, true);
                }

                // Finish
                File zip = external.zipBuffered(8192, new BackupFileFilter());
                zip = external.rename(zip, EXPORT_FILENAME);

                // Move to our temporary file cache to not pollute disk space
                internal.copy(zip, outputSmrBackupFile, true);
                external.delete(zip);

                return outputSmrBackupFile;

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
