package com.wops.receiptsgo.sync.manual;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.persistence.database.restore.DatabaseRestorer;
import com.wops.analytics.log.Logger;
import com.wops.core.persistence.DatabaseConstants;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.StorageManager;

@ApplicationScope
public class ManualRestoreTask {

    private final Context context;
    private final PersistenceManager persistenceManager;
    private final DatabaseRestorer databaseRestorer;
    private final Scheduler observeOnScheduler;
    private final Scheduler subscribeOnScheduler;
    private final Map<RestoreRequest, CompletableSubject> restoreSubjectMap = new HashMap<>();

    @Inject
    ManualRestoreTask(@NonNull Context context,
                      @NonNull PersistenceManager persistenceManager,
                      @NonNull DatabaseRestorer databaseRestorer) {
        this(context, persistenceManager, databaseRestorer, Schedulers.io(), Schedulers.io());
    }

    private ManualRestoreTask(@NonNull Context context,
                              @NonNull PersistenceManager persistenceManager,
                              @NonNull DatabaseRestorer databaseRestorer,
                              @NonNull Scheduler observeOnScheduler,
                              @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.persistenceManager = Preconditions.checkNotNull(persistenceManager);
        this.databaseRestorer = Preconditions.checkNotNull(databaseRestorer);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @NonNull
    public synchronized Completable restoreData(@NonNull Uri uri, boolean overwrite) {
        final RestoreRequest restoreRequest = new RestoreRequest(uri, overwrite);
        CompletableSubject restoreReplaySubject = restoreSubjectMap.get(restoreRequest);
        if (restoreReplaySubject == null) {
            restoreReplaySubject = CompletableSubject.create();
            restoreDataToSingle(uri, overwrite)
                    .observeOn(observeOnScheduler)
                    .subscribeOn(subscribeOnScheduler)
                    .subscribe(restoreReplaySubject);
            restoreSubjectMap.put(restoreRequest, restoreReplaySubject);
        }
        return restoreReplaySubject;
    }

    public synchronized void markRestorationAsComplete(@NonNull Uri uri, boolean overwrite) {
        final RestoreRequest restoreRequest = new RestoreRequest(uri, overwrite);
        restoreSubjectMap.remove(restoreRequest);
    }

    @NonNull
    private Completable restoreDataToSingle(@NonNull final Uri uri, final boolean overwrite) {
        return copyBackupToLocalPath(uri)
                .doOnSubscribe(ignored -> {
                    Logger.debug(this, "Starting log task at {}", System.currentTimeMillis());
                    Logger.debug(this, "Uri: {}", uri);
                })
                .flatMapCompletable(localZipFile -> {
                    //noinspection CodeBlock2Expr
                    return unzipAllFilesAndGetImportDatabaseFile(localZipFile, overwrite)
                            .flatMapCompletable(importDatabaseFile -> databaseRestorer.restoreDatabase(importDatabaseFile, overwrite));
                })
                .doOnError(error ->
                        Logger.error(ManualRestoreTask.this, "Caught exception during import.", error));

    }

    @NonNull
    private Single<File> copyBackupToLocalPath(@NonNull final Uri uri) {
        return Single.fromCallable(() -> {
            Logger.debug(this, "Deleting existing backup database...");
            final SDCardFileManager external = persistenceManager.getExternalStorageManager();
            //noinspection ResultOfMethodCallIgnored
            external.getFile(DatabaseConstants.DATABASE_EXPORT_NAME).delete();

            Logger.debug(this, "Deleting existing import zip...");
            final File localZipFile = external.getFile("smart.zip");
            //noinspection ResultOfMethodCallIgnored
            external.delete(localZipFile);

            final String scheme = uri.getScheme();
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                Logger.debug(this, "Processing URI with content scheme.");
                InputStream inputStream = null;
                try {
                    ContentResolver cr = context.getContentResolver();
                    inputStream = cr.openInputStream(uri);

                    if (!external.copy(inputStream, localZipFile, true)) {
                        throw new IOException("Failed to copy our import data to " + localZipFile.getAbsolutePath());
                    } else {
                        return localZipFile;
                    }
                } finally {
                    StorageManager.closeQuietly(inputStream);
                }
            } else {
                Logger.debug(this, "Processing URI with unknown scheme.");
                File src = null;

                if (uri.getPath() != null) {
                    src = new File(uri.getPath());
                } else if (uri.getEncodedPath() != null) {
                    src = new File(uri.getEncodedPath());
                }

                // Validate that we have a valid source
                if (src == null || !src.exists()) {
                    Logger.debug(ManualRestoreTask.this, "Failed to parse uri scheme: {}.", src);
                    throw new IOException("Failed to validate the uri: " + uri);
                }

                if (!external.copy(src, localZipFile, true)) {
                    throw new IOException("Failed to copy our import data to " + localZipFile.getAbsolutePath());
                } else {
                    return localZipFile;
                }
            }
        })
        .doOnSuccess(wasCopySuccessful ->
                Logger.info(ManualRestoreTask.this, "Successfully copied our backup to our local path"));
    }

    @NonNull
    private Single<File> unzipAllFilesAndGetImportDatabaseFile(@NonNull File localZipFile, final boolean overwrite) {
        return Single.fromCallable(() -> {
            final SDCardFileManager external = persistenceManager.getExternalStorageManager();
            StorageManager internal = persistenceManager.getInternalStorageManager();

            if (!external.unzip(localZipFile, overwrite)) {
                throw new IOException("Failed to unzip file: " + localZipFile);
            }

            Logger.info(ManualRestoreTask.this, "Importing shared preferences");
            final File sdPrefs = external.getFile("shared_prefs");
            final File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");
            if (!internal.copy(sdPrefs, prefs, overwrite)) {
                throw new IOException("Failed to import settings");
            }

            Logger.info(ManualRestoreTask.this, "Importing internal files");
            final File internalDir = external.getFile("Internal");
            if (!internal.copy(internalDir, internal.getRoot(), overwrite)) {
                // Note: we don't treat this as a critical error that should stop our import process
                Logger.error(ManualRestoreTask.this, "Failed to import local files");
            }

            final File importDatabaseFile = external.getFile(DatabaseConstants.DATABASE_EXPORT_NAME);
            if (!importDatabaseFile.exists()) {
                throw new IOException("Failed to find our import database file");
            }

            // delete the zip file since we've successfully exported the data from it
            external.delete(localZipFile);

            return importDatabaseFile;
        })
        .doOnSuccess(importDatabaseFile ->
                Logger.info(ManualRestoreTask.this, "Successfully unzipped our backup and configured all local files"));
    }

    private static final class RestoreRequest {

        private final Uri uri;
        private final boolean overwrite;

        RestoreRequest(@NonNull Uri uri, boolean overwrite) {
            this.uri = Preconditions.checkNotNull(uri);
            this.overwrite = overwrite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RestoreRequest)) return false;

            RestoreRequest that = (RestoreRequest) o;

            if (overwrite != that.overwrite) return false;
            return uri.equals(that.uri);

        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + (overwrite ? 1 : 0);
            return result;
        }
    }
}
