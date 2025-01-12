package com.wops.receiptsgo.sync.drive.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.restore.DatabaseRestorer;
import com.wops.receiptsgo.persistence.database.tables.AbstractSqlTable;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import com.wops.receiptsgo.persistence.database.tables.TripsTable;
import com.wops.receiptsgo.sync.errors.MissingFilesException;
import com.wops.automatic_backups.drive.managers.DriveDatabaseManager;
import com.wops.automatic_backups.drive.managers.DriveDownloader;
import com.wops.core.persistence.DatabaseConstants;
import com.wops.core.sync.model.RemoteBackupMetadata;
import com.wops.core.sync.model.impl.Identifier;
import io.reactivex.Observable;
import io.reactivex.Single;

public class DriveRestoreDataManager {

    private static final Integer ALLOWED_DOWNLOAD_FAILURES = 10;

    private final DriveDatabaseManager mDriveDatabaseManager;
    private final DatabaseRestorer databaseRestorer;
    private final File mStorageDirectory;
    private final DriveDownloader driveDownloader;

    @SuppressWarnings("ConstantConditions")
    public DriveRestoreDataManager(@NonNull Context context,
                                   @NonNull DriveDatabaseManager driveDatabaseManager,
                                   @NonNull DatabaseRestorer databaseRestorer,
                                   @NonNull DriveDownloader driveDownloader) {
        this(driveDatabaseManager, databaseRestorer, context.getExternalFilesDir(null), driveDownloader);
    }

    private DriveRestoreDataManager(@NonNull DriveDatabaseManager driveDatabaseManager,
                                    @NonNull DatabaseRestorer databaseRestorer,
                                    @NonNull File storageDirectory,
                                    @NonNull DriveDownloader driveDownloader) {
        this.mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        this.databaseRestorer = Preconditions.checkNotNull(databaseRestorer);
        this.mStorageDirectory = Preconditions.checkNotNull(storageDirectory);
        this.driveDownloader = Preconditions.checkNotNull(driveDownloader);
    }

    @NonNull
    public Single<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        Logger.info(this, "Initiating the restoration of a backup file for Google Drive with ID: {}", remoteBackupMetadata.getId());

        return downloadBackupMetadataImages(remoteBackupMetadata, overwriteExistingData, mStorageDirectory)
                .flatMap(files -> {
                    Logger.debug(this, "Performing database merge");
                    final File tempDbFile = new File(mStorageDirectory, DatabaseConstants.DATABASE_EXPORT_NAME);
                    return databaseRestorer.restoreDatabase(tempDbFile, overwriteExistingData)
                            .toSingleDefault(true);
                })
                .doOnSuccess(aBoolean -> {
                    Logger.debug(this, "Syncing database following merge operation");
                    mDriveDatabaseManager.syncDatabase();
                });
    }

    @NonNull
    public Single<List<File>> downloadAllBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation) {
        return downloadBackupMetadataImages(remoteBackupMetadata, true, downloadLocation);
    }

    @NonNull
    public Single<List<File>> downloadAllFilesInDriveFolder(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return driveDownloader.downloadAllFilesInDriveFolder(remoteBackupMetadata, downloadLocation);
    }

    @NonNull
    private Single<List<File>> downloadBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData,
                                                            @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        AtomicInteger missingFileCount = new AtomicInteger();

        return deletePreviousTemporaryDatabase(downloadLocation)
                .filter(success -> success)
                .flatMapSingle(ignored -> driveDownloader.downloadTmpDatabaseFile(remoteBackupMetadata, downloadLocation))
                .flatMapObservable(file -> {
                    Logger.debug(DriveRestoreDataManager.this, "Retrieving partial receipts from our temporary drive database");
                    return getPartialReceipts(file.get());
                })
                .flatMapSingle(partialReceipt -> {
                    Logger.debug(DriveRestoreDataManager.this, "Creating trip folder for partial receipt: {}", partialReceipt.parentTripName);
                    return createParentFolderIfNeeded(partialReceipt, downloadLocation);
                })
                .filter(partialReceipt -> {
                    if (overwriteExistingData) {
                        return true;
                    } else {
                        final File receiptFile = new File(new File(downloadLocation, partialReceipt.parentTripName), partialReceipt.fileName);
                        Logger.debug(DriveRestoreDataManager.this, "Filtering out receipt? " + !receiptFile.exists());
                        return !receiptFile.exists();
                    }
                })
                .flatMapSingle(partialReceipt -> {
                    Logger.debug(DriveRestoreDataManager.this, "Downloading file for partial receipt: {}", partialReceipt.driveId);
                    Single<Optional<java.io.File>> singleOptional = downloadFileForReceipt(partialReceipt, downloadLocation);
                    Optional<java.io.File> optionalFile = singleOptional.blockingGet();
                    if (!optionalFile.isPresent()) {
                        missingFileCount.getAndIncrement();
                        if (missingFileCount.get() > ALLOWED_DOWNLOAD_FAILURES) {
                            return Single.error(new MissingFilesException("Aborting import: More than five missing files"));
                        }
                    }
                    return singleOptional;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Single<Boolean> deletePreviousTemporaryDatabase(@NonNull final File inDirectory) {
        return Single.create(emitter -> {
            final File tempDbFile = new File(inDirectory, DatabaseConstants.DATABASE_EXPORT_NAME);
            if (tempDbFile.exists()) {
                if (tempDbFile.delete()) {
                    emitter.onSuccess(true);
                } else {
                    emitter.onError(new IOException("Failed to delete our temporary database file"));
                }
            } else {
                emitter.onSuccess(true);
            }
        });
    }

    private Observable<PartialReceipt> getPartialReceipts(@NonNull final File temporaryDatabaseFile) {
        Preconditions.checkNotNull(temporaryDatabaseFile);

        return Observable.fromCallable(() -> {
            try (SQLiteDatabase importDb = SQLiteDatabase.openDatabase(temporaryDatabaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                final List<PartialReceipt> partialReceipts = new ArrayList<>();
                if (importDb.getVersion() < 19) { // using old approach (receipts reference to trip name)
                    final String[] selection = new String[]{AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH};
                    try (Cursor receiptsCursor = importDb.query(ReceiptsTable.TABLE_NAME, selection, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " IS NOT NULL AND " + ReceiptsTable.COLUMN_PATH + " IS NOT NULL AND " + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(0)}, null, null, null)) {
                        if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
                            final int driveIdIndex = receiptsCursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
                            final int parentIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                            final int pathIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);

                            do {
                                final String driveId = receiptsCursor.getString(driveIdIndex);
                                final String parent = receiptsCursor.getString(parentIndex);
                                final String path = receiptsCursor.getString(pathIndex);
                                if (driveId != null && parent != null && !TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
                                    partialReceipts.add(new PartialReceipt(driveId, parent, path));
                                }
                            }
                            while (receiptsCursor.moveToNext());
                        }
                    }
                } else { // using new approach (receipts reference to trip id)
                    final String[] selection = new String[]{AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, ReceiptsTable.COLUMN_PARENT_TRIP_ID, ReceiptsTable.COLUMN_PATH};
                    try (Cursor receiptsCursor = importDb.query(ReceiptsTable.TABLE_NAME, selection, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " IS NOT NULL AND " + ReceiptsTable.COLUMN_PATH + " IS NOT NULL AND " + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(0)}, null, null, null)) {
                        if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
                            final int driveIdIndex = receiptsCursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
                            final int parentIdIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT_TRIP_ID);
                            final int pathIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                            do {
                                final String driveId = receiptsCursor.getString(driveIdIndex);
                                final String path = receiptsCursor.getString(pathIndex);
                                final int parentId = receiptsCursor.getInt(parentIdIndex);

                                // getting trip name by id (trip name is unique column)
                                try (Cursor tripsCursor = importDb.query(TripsTable.TABLE_NAME, new String[]{TripsTable.COLUMN_NAME},
                                        TripsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(parentId)}, null, null, null, "1")) {

                                    if (tripsCursor != null && tripsCursor.moveToFirst()) {
                                        final int tripNameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
                                        final String tripName = tripsCursor.getString(tripNameIndex);

                                        if (driveId != null && tripName != null && !TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
                                            partialReceipts.add(new PartialReceipt(driveId, tripName, path));
                                        }
                                    }
                                }
                            }
                            while (receiptsCursor.moveToNext());
                        }
                    }
                }
                return partialReceipts;
            }
        }).flatMapIterable(items -> items);
    }

    private Single<PartialReceipt> createParentFolderIfNeeded(@NonNull final PartialReceipt partialReceipt, @NonNull final File inDirectory) {
        return Single.create(emitter -> {
            final File parentTripFolder = new File(inDirectory, partialReceipt.parentTripName);
            if (!parentTripFolder.exists()) {
                if (parentTripFolder.mkdir()) {
                    emitter.onSuccess(partialReceipt);
                } else {
                    emitter.onError(new IOException("Failed to create the parent directory for this receipt"));
                }
            } else {
                emitter.onSuccess(partialReceipt);
            }
        });
    }

    private Single<Optional<File>> downloadFileForReceipt(@NonNull final PartialReceipt partialReceipt, @NonNull final File inDirectory) {
        final File receiptFile = new File(new File(inDirectory, partialReceipt.parentTripName), partialReceipt.fileName);
        return driveDownloader.downloadFile(partialReceipt.driveId.getId(), receiptFile)
                .doOnError(throwable -> Logger.error(DriveRestoreDataManager.this, "Failed to download {} in {} with id {}.", partialReceipt.fileName, partialReceipt.parentTripName, partialReceipt.driveId));
    }

    /**
     * A subset of receipt metadata so we don't need to full new as many objects as normally required,
     * since this will have a lot of extra memory overhead
     */
    private static final class PartialReceipt {
        private final Identifier driveId;
        private final String parentTripName;
        private final String fileName;

        PartialReceipt(@NonNull String driveId, @NonNull String parentTripName, @NonNull String fileName) {
            this.driveId = new Identifier(driveId);
            this.parentTripName = Preconditions.checkNotNull(parentTripName);
            this.fileName = Preconditions.checkNotNull(fileName);
        }
    }
}
