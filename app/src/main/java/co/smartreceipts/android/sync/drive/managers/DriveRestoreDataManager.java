package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.android.gms.drive.DriveId;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.restore.DatabaseRestorer;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.manual.ManualBackupTask;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;


public class DriveRestoreDataManager {

    private final Context mContext;
    private final DriveStreamsManager mDriveStreamsManager;
    private final DriveDatabaseManager mDriveDatabaseManager;
    private final DatabaseHelper mDatabaseHelper;
    private final DatabaseRestorer databaseRestorer;
    private final File mStorageDirectory;

    @SuppressWarnings("ConstantConditions")
    public DriveRestoreDataManager(@NonNull Context context,
                                   @NonNull DriveStreamsManager driveStreamsManager,
                                   @NonNull DatabaseHelper databaseHelper,
                                   @NonNull DriveDatabaseManager driveDatabaseManager,
                                   @NonNull DatabaseRestorer databaseRestorer) {
        this(context, driveStreamsManager, databaseHelper, driveDatabaseManager, databaseRestorer, context.getExternalFilesDir(null));
    }

    public DriveRestoreDataManager(@NonNull Context context,
                                   @NonNull DriveStreamsManager driveStreamsManager,
                                   @NonNull DatabaseHelper databaseHelper,
                                   @NonNull DriveDatabaseManager driveDatabaseManager,
                                   @NonNull DatabaseRestorer databaseRestorer,
                                   @NonNull File storageDirectory) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveStreamsManager = Preconditions.checkNotNull(driveStreamsManager);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
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
                    final File tempDbFile = new File(mStorageDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
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

        return mDriveStreamsManager.getDriveId(remoteBackupMetadata.getId())
                .map(driveId -> {
                    Logger.debug(DriveRestoreDataManager.this, "Converting drive id to smart receipts drive folder");
                    return driveId.asDriveFolder();
                })
                .flatMapObservable(mDriveStreamsManager::getFilesInFolder)
                .map(DriveId::asDriveFile)
                .flatMapSingle(driveFile -> mDriveStreamsManager.getMetadata(driveFile)
                        .map(metadata -> driveFile.getDriveId().getResourceId() + "__" + metadata.getOriginalFilename())
                        .flatMap(filename -> mDriveStreamsManager.download(driveFile, new File(downloadLocation, filename))))
                .toList();
    }

    @VisibleForTesting
    @NonNull
    public Single<List<File>> downloadFilesAfterDate(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 03, 01);
        final Date date = calendar.getTime();
        return mDriveStreamsManager.getAllFiles()
                .map(DriveId::asDriveFile)
                .flatMapMaybe(driveFile -> {
                    return mDriveStreamsManager.getMetadata(driveFile)
                            .map(Optional::of)
                            .onErrorReturnItem(Optional.absent())
                            .flatMapObservable(metadata -> {
                                if (metadata.isPresent()) {
                                    return Observable.just(metadata.get());
                                } else {
                                    return Observable.empty();
                                }
                            })
                            .flatMap(metadata -> {
                                if (metadata.getModifiedDate().after(date)) {
                                    return Observable.just(metadata);
                                } else {
                                    return Observable.empty();
                                }
                            })
                            .map(metadata -> {
                                return ModelUtils.getFormattedDate(metadata.getModifiedDate(), TimeZone.getDefault(), mContext, "-") + "_" + metadata.getOriginalFilename();
                            })
                            .flatMapSingle(filename -> mDriveStreamsManager.download(driveFile, new File(downloadLocation, filename)))
                            .firstElement();
                })
                .toList();

    }


    @NonNull
    private Single<List<File>> downloadBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData,
                                                            @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return deletePreviousTemporaryDatabase(downloadLocation)
                .<Optional<DriveId>>flatMap(success -> {
                    if (success) {
                        Logger.debug(DriveRestoreDataManager.this, "Fetching drive id");
                        return mDriveStreamsManager.getDriveId(remoteBackupMetadata.getId()).map(Optional::of);
                    } else {
                        return Single.just(Optional.absent());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMapSingle(driveId -> {
                    Logger.debug(DriveRestoreDataManager.this, "Converting drive id to smart receipts drive folder");
                    return Single.just(driveId.asDriveFolder());
                })
                .flatMapObservable(driveFolder -> {
                    Logger.debug(DriveRestoreDataManager.this, "Fetching receipts database in drive for this folder");
                    return mDriveStreamsManager.getFilesInFolder(driveFolder, DatabaseHelper.DATABASE_NAME);
                })
                .take(1)
                .flatMap(driveId -> {
                    Logger.debug(DriveRestoreDataManager.this, "Converting database drive id to drive file");
                    return Observable.just(driveId.asDriveFile());
                })
                .flatMapSingle(driveFile -> {
                    Logger.debug(DriveRestoreDataManager.this, "Downloading database file");
                    final File tempDbFile = new File(downloadLocation, ManualBackupTask.DATABASE_EXPORT_NAME);
                    return mDriveStreamsManager.download(driveFile, tempDbFile);
                })
                .flatMap(file -> {
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
                        final File receiptFile = new File(new File(downloadLocation, partialReceipt.parentTripName), partialReceipt.fileName);
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

    private Single<Boolean> deletePreviousTemporaryDatabase(@NonNull final File inDirectory) {
        return Single.create(emitter -> {
            final File tempDbFile = new File(inDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
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

        return Observable.create(emitter -> {
            SQLiteDatabase importDb = null;
            Cursor receiptsCursor = null;
            Cursor tripsCursor = null;
            try {
                importDb = SQLiteDatabase.openDatabase(temporaryDatabaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

                if (importDb.getVersion() < 19) { // using old approach (receipts reference to trip name)
                    final String[] selection = new String[]{AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH};
                    receiptsCursor = importDb.query(ReceiptsTable.TABLE_NAME, selection, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " IS NOT NULL AND " + ReceiptsTable.COLUMN_PATH + " IS NOT NULL AND " + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(0)}, null, null, null);
                    if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
                        final int driveIdIndex = receiptsCursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
                        final int parentIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                        final int pathIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);

                        do {
                            final String driveId = receiptsCursor.getString(driveIdIndex);
                            final String parent = receiptsCursor.getString(parentIndex);
                            final String path = receiptsCursor.getString(pathIndex);
                            if (driveId != null && parent != null && !TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
                                emitter.onNext(new PartialReceipt(driveId, parent, path));
                            }
                        }
                        while (receiptsCursor.moveToNext());
                    }
                } else { // using new approach (receipts reference to trip id)
                    final String[] selection = new String[]{AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, ReceiptsTable.COLUMN_PARENT_TRIP_ID, ReceiptsTable.COLUMN_PATH};
                    receiptsCursor = importDb.query(ReceiptsTable.TABLE_NAME, selection, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " IS NOT NULL AND " + ReceiptsTable.COLUMN_PATH + " IS NOT NULL AND " + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(0)}, null, null, null);
                    if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
                        final int driveIdIndex = receiptsCursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
                        final int parentIdIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT_TRIP_ID);
                        final int pathIndex = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                        do {
                            final String driveId = receiptsCursor.getString(driveIdIndex);
                            final String path = receiptsCursor.getString(pathIndex);
                            final int parentId = receiptsCursor.getInt(parentIdIndex);

                            // getting trip name by id (trip name is unique column)
                            tripsCursor = importDb.query(TripsTable.TABLE_NAME, new String[]{TripsTable.COLUMN_NAME},
                                    TripsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(parentId)}, null, null, null, "1");

                            if (tripsCursor != null && tripsCursor.moveToFirst()) {
                                final int tripNameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
                                final String tripName = tripsCursor.getString(tripNameIndex);

                                if (driveId != null && tripName != null && !TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
                                    emitter.onNext(new PartialReceipt(driveId, tripName, path));
                                }
                            }

                        }
                        while (receiptsCursor.moveToNext());
                    }
                }
                emitter.onComplete();
            } finally {
                if (importDb != null) {
                    importDb.close();
                }
                if (receiptsCursor != null) {
                    receiptsCursor.close();
                }
                if (tripsCursor != null) {
                    tripsCursor.close();
                }
            }
        });
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
        return mDriveStreamsManager.getDriveId(partialReceipt.driveId)
                .flatMap(driveId -> Single.just(driveId.asDriveFile()))
                .flatMap(driveFile -> {
                    final File receiptFile = new File(new File(inDirectory, partialReceipt.parentTripName), partialReceipt.fileName);
                    return mDriveStreamsManager.download(driveFile, receiptFile);
                })
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
