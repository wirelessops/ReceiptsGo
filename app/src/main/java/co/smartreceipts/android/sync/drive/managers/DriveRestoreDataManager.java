package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.restore.DatabaseRestorer;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.manual.ManualBackupTask;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import io.reactivex.Observable;
import io.reactivex.Single;

public class DriveRestoreDataManager {

    private final Context mContext;
    private final DriveStreamsManager mDriveStreamsManager;
    private final DriveDatabaseManager mDriveDatabaseManager;
    private final DatabaseRestorer databaseRestorer;
    private final java.io.File mStorageDirectory;

    @SuppressWarnings("ConstantConditions")
    public DriveRestoreDataManager(@NonNull Context context,
                                   @NonNull DriveStreamsManager driveStreamsManager,
                                   @NonNull DriveDatabaseManager driveDatabaseManager,
                                   @NonNull DatabaseRestorer databaseRestorer) {
        this(context, driveStreamsManager, driveDatabaseManager, databaseRestorer, context.getExternalFilesDir(null));
    }

    public DriveRestoreDataManager(@NonNull Context context,
                                   @NonNull DriveStreamsManager driveStreamsManager,
                                   @NonNull DriveDatabaseManager driveDatabaseManager,
                                   @NonNull DatabaseRestorer databaseRestorer,
                                   @NonNull java.io.File storageDirectory) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveStreamsManager = Preconditions.checkNotNull(driveStreamsManager);
        mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        this.databaseRestorer = Preconditions.checkNotNull(databaseRestorer);
        mStorageDirectory = Preconditions.checkNotNull(storageDirectory);
    }

    @NonNull
    public Single<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        Logger.info(this, "Initiating the restoration of a backup file for Google Drive with ID: {}", remoteBackupMetadata.getId());

        return downloadBackupMetadataImages(remoteBackupMetadata, overwriteExistingData, mStorageDirectory)
                .flatMap(files -> {
                    Logger.debug(this, "Performing database merge");
                    final java.io.File tempDbFile = new java.io.File(mStorageDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
                    return databaseRestorer.restoreDatabase(tempDbFile, overwriteExistingData)
                            .toSingleDefault(true);
                })
                .doOnSuccess(aBoolean -> {
                    Logger.debug(this, "Syncing database following merge operation");
                    mDriveDatabaseManager.syncDatabase();
                });
    }

    @NonNull
    public Single<List<java.io.File>> downloadAllBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final java.io.File downloadLocation) {
        return downloadBackupMetadataImages(remoteBackupMetadata, true, downloadLocation);
    }

    @NonNull
    public Single<List<java.io.File>> downloadAllFilesInDriveFolder(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final java.io.File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return mDriveStreamsManager.getFilesInFolder(remoteBackupMetadata.getId().getId())
                .flatMap(fileList -> {
                    List<java.io.File> javaFileList = new ArrayList<>();
                    for (File file : fileList.getFiles()) {
                        String filename = file.getId() + "__" + file.getOriginalFilename();
                        javaFileList.add(mDriveStreamsManager.download(file.getId(), new java.io.File(downloadLocation, filename)).blockingGet());
                    }
                    return Single.just(javaFileList);
                });
    }

    @VisibleForTesting
    @NonNull
    public Single<List<java.io.File>> downloadFilesAfterDate(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final java.io.File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 03, 01);
        final Date date = calendar.getTime();
        return mDriveStreamsManager.getAllFiles()
                .flatMap(fileList -> {
                    List<java.io.File> javaFileList = new ArrayList<>();
                    for (File f : fileList.getFiles()) {
                        if (f.getModifiedTime().getValue() > date.getTime()) {
                            String filename = ModelUtils.getFormattedDate(
                                    new Date(f.getModifiedTime().getValue()), TimeZone.getDefault(), mContext, "-") + "_" + f.getOriginalFilename();
                            javaFileList.add(mDriveStreamsManager.download(f.getId(), new java.io.File(downloadLocation, filename)).blockingGet());
                        }
                    }
                    return Single.just(javaFileList);
                });
    }

    @NonNull
    private Single<List<java.io.File>> downloadBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData,
                                                                    @NonNull final java.io.File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return deletePreviousTemporaryDatabase(downloadLocation)
                .<Optional<FileList>>flatMap(success -> {
                    if (success) {
                        Logger.debug(DriveRestoreDataManager.this, "Fetching receipts database in drive for this folder");
                        return mDriveStreamsManager.getFilesInFolder(remoteBackupMetadata.getId().getId(), DatabaseHelper.DATABASE_NAME).map(Optional::of);
                    } else {
                        return Single.just(Optional.absent());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMapSingle(fileList -> {
                    Logger.debug(DriveRestoreDataManager.this, "Downloading database file");
                    final java.io.File tempDbFile = new java.io.File(downloadLocation, ManualBackupTask.DATABASE_EXPORT_NAME);
                    return mDriveStreamsManager.download(fileList.getFiles().get(0).getId(), tempDbFile);
                })
                .flatMapObservable(file -> {
                    Logger.debug(DriveRestoreDataManager.this, "Retrieving partial receipts from our temporary drive database");
                    return getPartialReceipts(file);
                })
                .flatMapSingle(partialReceipt -> {
                    Logger.debug(DriveRestoreDataManager.this, "Creating trip folder for partial receipt: {}", partialReceipt.parentTripName);
                    return createParentFolderIfNeeded(partialReceipt, downloadLocation);
                })
                .filter(partialReceipt -> {
                    if (overwriteExistingData) {
                        return true;
                    } else {
                        final java.io.File receiptFile = new java.io.File(new java.io.File(downloadLocation, partialReceipt.parentTripName), partialReceipt.fileName);
                        Logger.debug(DriveRestoreDataManager.this, "Filtering out receipt? " + !receiptFile.exists());
                        return !receiptFile.exists();
                    }
                })
                .flatMapSingle(partialReceipt -> {
                    Logger.debug(DriveRestoreDataManager.this, "Downloading file for partial receipt: {}", partialReceipt.driveId);
                    return downloadFileForReceipt(partialReceipt, downloadLocation);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Single<Boolean> deletePreviousTemporaryDatabase(@NonNull final java.io.File inDirectory) {
        return Single.create(emitter -> {
            final java.io.File tempDbFile = new java.io.File(inDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
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

    private Observable<PartialReceipt> getPartialReceipts(@NonNull final java.io.File temporaryDatabaseFile) {
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

    private Single<PartialReceipt> createParentFolderIfNeeded(@NonNull final PartialReceipt partialReceipt, @NonNull final java.io.File inDirectory) {
        return Single.create(emitter -> {
            final java.io.File parentTripFolder = new java.io.File(inDirectory, partialReceipt.parentTripName);
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

    private Single<Optional<java.io.File>> downloadFileForReceipt(@NonNull final PartialReceipt partialReceipt, @NonNull final java.io.File inDirectory) {
        final java.io.File receiptFile = new java.io.File(new java.io.File(inDirectory, partialReceipt.parentTripName), partialReceipt.fileName);
        return mDriveStreamsManager.download(partialReceipt.driveId.getId(), receiptFile)
                .map(Optional::of)
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

        public PartialReceipt(@NonNull String driveId, @NonNull String parentTripName, @NonNull String fileName) {
            this.driveId = new Identifier(driveId);
            this.parentTripName = Preconditions.checkNotNull(parentTripName);
            this.fileName = Preconditions.checkNotNull(fileName);
        }
    }
}
