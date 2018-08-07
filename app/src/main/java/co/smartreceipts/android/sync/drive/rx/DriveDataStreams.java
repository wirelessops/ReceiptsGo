package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.DeviceMetadata;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.debug.DriveFilesAndFoldersPrinter;
import co.smartreceipts.android.sync.drive.services.DriveIdUploadCompleteCallback;
import co.smartreceipts.android.sync.drive.services.DriveIdUploadMetadata;
import co.smartreceipts.android.sync.drive.services.DriveUploadCompleteManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.DefaultRemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import wb.android.storage.StorageManager;

class DriveDataStreams {

    private static final String SMART_RECEIPTS_FOLDER = "Smart Receipts";
    private static final int BYTE_BUFFER_SIZE = 8192;

    /**
     * Saves the randomly generated UDID that is associated with this device. We leverage this in order to determine
     * if this is a "new" install (even on the same device) or is an existing sync for this device.
     */
    private static final CustomPropertyKey SMART_RECEIPTS_FOLDER_KEY = new CustomPropertyKey("smart_receipts_id", CustomPropertyKey.PUBLIC);

    private final Context context;
    private final DriveClient driveClient;
    private final DriveResourceClient driveResourceClient;
    private final GoogleDriveSyncMetadata googleDriveSyncMetadata;
    private final DeviceMetadata deviceMetadata;
    private final DriveUploadCompleteManager driveUploadCompleteManager;

    private ReplaySubject<DriveFolder> smartReceiptsFolderSubject;

    public DriveDataStreams(@NonNull Context context,
                            @NonNull DriveClient driveClient,
                            @NonNull DriveResourceClient driveResourceClient,
                            @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                            @NonNull DriveUploadCompleteManager driveUploadCompleteManager) {
        this(context, driveClient, driveResourceClient, googleDriveSyncMetadata, new DeviceMetadata(context), driveUploadCompleteManager);
    }

    public DriveDataStreams(@NonNull Context context,
                            @NonNull DriveClient driveClient,
                            @NonNull DriveResourceClient driveResourceClient,
                            @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                            @NonNull DeviceMetadata deviceMetadata,
                            @NonNull DriveUploadCompleteManager driveUploadCompleteManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.driveClient = Preconditions.checkNotNull(driveClient);
        this.driveResourceClient = Preconditions.checkNotNull(driveResourceClient);
        this.googleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        this.deviceMetadata = Preconditions.checkNotNull(deviceMetadata);
        this.driveUploadCompleteManager = Preconditions.checkNotNull(driveUploadCompleteManager);
    }

    @NonNull
    public synchronized Single<List<RemoteBackupMetadata>> getSmartReceiptsFolders() {
        final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, SMART_RECEIPTS_FOLDER)).build();
        return RxDriveTask.toSingle(driveResourceClient.query(folderQuery))
                .flatMap(metadataBuffer -> {
                    try {
                        final List<Metadata> folderMetadataList = new ArrayList<>();
                        for (final Metadata metadata : metadataBuffer) {
                            if (isValidSmartReceiptsFolder(metadata)) {
                                Logger.info(DriveDataStreams.this, "Tentatively found a Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                                folderMetadataList.add(metadata);
                            } else {
                                Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                            }
                        }
                        return Single.just(folderMetadataList);
                    } finally {
                        metadataBuffer.release();
                    }
                })
                .flatMap(metadataList -> {
                    final Query databaseQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, DatabaseHelper.DATABASE_NAME)).build();
                    return Observable.fromIterable(metadataList)
                            .flatMap(metadata -> {
                                final DriveFolder driveFolder = metadata.getDriveId().asDriveFolder();
                                return RxDriveTask.toObservable(driveResourceClient.queryChildren(driveFolder, databaseQuery))
                                        .map(databaseMetadataBuffer -> {
                                           try {
                                               // Get the folder resource id
                                               final String validResourceId = metadata.getDriveId().getResourceId();
                                               //noinspection ConstantConditions
                                               final Identifier driveFolderId = new Identifier(validResourceId);
                                               final Map<CustomPropertyKey, String> customPropertyMap = metadata.getCustomProperties();
                                               Logger.debug(DriveDataStreams.this, "Found existing Smart Receipts folder with id: {}", driveFolderId);

                                               // Get the device id for the device that created this backup
                                               final Identifier syncDeviceIdentifier;
                                               if (customPropertyMap != null && customPropertyMap.containsKey(SMART_RECEIPTS_FOLDER_KEY)) {
                                                   syncDeviceIdentifier = new Identifier(customPropertyMap.get(SMART_RECEIPTS_FOLDER_KEY));
                                                   Logger.info(DriveDataStreams.this, "Found valid Smart Receipts folder a known device id");
                                               } else {
                                                   syncDeviceIdentifier = new Identifier("UnknownDevice");
                                                   Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder without a tagged device key");
                                               }

                                               // Get the metadata (i.e. device name like Pixel)
                                               final String deviceName = metadata.getDescription() != null ? metadata.getDescription() : "";

                                               // Set the last modified date, using the last database update
                                               Date lastModifiedDate = metadata.getModifiedDate();
                                               for (final Metadata databaseMetadata : databaseMetadataBuffer) {
                                                   if (databaseMetadata.getModifiedDate().getTime() > lastModifiedDate.getTime()) {
                                                       lastModifiedDate = databaseMetadata.getModifiedDate();
                                                   }
                                               }

                                               // Return all of this via our internal database wrapper
                                               return new DefaultRemoteBackupMetadata(driveFolderId, syncDeviceIdentifier, deviceName, lastModifiedDate);
                                           } finally {
                                               databaseMetadataBuffer.release();
                                           }
                                        })
                                        .doOnNext(backupMetadata -> Logger.debug(DriveDataStreams.this, "Successfully queried the backup metadata for the Smart Receipts folder with id: {}", backupMetadata.getId()))
                                        .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query a database within the parent folder: {}", driveFolder.getDriveId()));
                            })
                            .toList()
                            .flatMap(defaultRemoteBackupMetadataList -> {
                                // Note: We create this new list to cast to the interface variant of the list
                                return Single.just(new ArrayList<RemoteBackupMetadata>(defaultRemoteBackupMetadataList));
                            });
                });
    }

    @NonNull
    public synchronized Observable<DriveFolder> getSmartReceiptsFolder() {
        if (smartReceiptsFolderSubject == null) {
            Logger.info(this, "Creating new replay subject for the Smart Receipts folder");
            smartReceiptsFolderSubject = ReplaySubject.create();

            final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SMART_RECEIPTS_FOLDER_KEY, googleDriveSyncMetadata.getDeviceIdentifier().getId())).build();
            RxDriveTask.toSingle(driveResourceClient.query(folderQuery))
                    .map(metadataBuffer -> {
                        try {
                            DriveId folderId = null;
                            for (final Metadata metadata : metadataBuffer) {
                                if (isValidSmartReceiptsFolder(metadata)) {
                                    folderId = metadata.getDriveId();
                                    break;
                                } else {
                                    Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                                }
                            }
                            return Optional.ofNullable(folderId);
                        } finally {
                            metadataBuffer.release();
                        }
                    })
                    .flatMap(driveIdOptional -> {
                        if (driveIdOptional.isPresent()) {
                            Logger.info(DriveDataStreams.this, "Found an existing Google Drive folder for Smart Receipts");
                            return Single.just(driveIdOptional.get().asDriveFolder());
                        } else {
                            Logger.info(DriveDataStreams.this, "Failed to find an existing Smart Receipts folder for this device. Creating a new one...");
                            final MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(SMART_RECEIPTS_FOLDER).setDescription(deviceMetadata.getDeviceName()).setCustomProperty(SMART_RECEIPTS_FOLDER_KEY, googleDriveSyncMetadata.getDeviceIdentifier().getId()).build();
                            return RxDriveTask.toSingle(driveResourceClient.getAppFolder())
                                    .flatMap(appFolder -> RxDriveTask.toSingle(driveResourceClient.createFolder(appFolder, changeSet)))
                                    .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to create a home folder with error: {}", throwable.getMessage()));
                        }
                    })
                    .toObservable()
                    .subscribe(smartReceiptsFolderSubject);
        }
        return smartReceiptsFolderSubject;
    }

    @NonNull
    public synchronized Single<DriveId> getDriveId(@NonNull final Identifier identifier) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(identifier, "The supplied identifier cannot be null");

        return RxDriveTask.toSingle(driveClient.getDriveId(identifier.getId()))
                .doOnSuccess(driveId -> Logger.debug(DriveDataStreams.this, "Successfully fetch file with id: {}", driveId))
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to fetch {} with status: {}", identifier, throwable.getMessage()));
    }

    @NonNull
    public synchronized Observable<DriveId> getAllFiles() {
        final SortOrder sortOrder = new SortOrder.Builder().addSortAscending(SortableField.MODIFIED_DATE).build();
        final Query query = new Query.Builder().setSortOrder(sortOrder).build();

        return RxDriveTask.toObservable(driveResourceClient.query(query))
                .compose(RxDriveTask.transformObservableMetadataBufferToList())
                .flatMapIterable(metadataList -> metadataList)
                .map(Metadata::getDriveId)
                .doOnError(throwable -> Logger.error(DriveFilesAndFoldersPrinter.class, "Failed to query with status: {}" , throwable.getMessage()));
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveFolder);

        final Query folderQuery = new Query.Builder().build();
        return RxDriveTask.toObservable(driveResourceClient.queryChildren(driveFolder, folderQuery))
                .compose(RxDriveTask.transformObservableMetadataBufferToList())
                .flatMapIterable(metadataList -> metadataList)
                .map(Metadata::getDriveId)
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query files in folder with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder, @NonNull final String fileName) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveFolder);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(fileName);

        final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, fileName)).build();
        return RxDriveTask.toObservable(driveResourceClient.queryChildren(driveFolder, folderQuery))
                .compose(RxDriveTask.transformObservableMetadataBufferToList())
                .flatMapIterable(metadataList -> metadataList)
                .map(Metadata::getDriveId)
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query files in folder by name with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<Metadata> getMetadata(@NonNull final DriveFile driveFile) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveFile);

        return RxDriveTask.toSingle(driveResourceClient.getMetadata(driveFile))
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to get metadata for file with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<List<Metadata>> getParents(@NonNull final DriveFile driveFile) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveFile);

        return RxDriveTask.toSingle(driveResourceClient.listParents(driveFile))
                .compose(RxDriveTask.transformSingleMetadataBufferToList())
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to get parents for file with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<DriveFile> createFileInFolder(@NonNull final DriveFolder folder, @NonNull final File file) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(folder);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(file);

        return RxDriveTask.toSingle(driveResourceClient.createContents())
                .flatMap(driveContents -> {
                    OutputStream outputStream = null;
                    FileInputStream fileInputStream = null;
                    try {
                        outputStream = driveContents.getOutputStream();
                        fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                        int read;
                        while ((read = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        final Uri uri = Uri.fromFile(file);
                        final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());
                        final MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                        builder.setTitle(file.getName());
                        if (!TextUtils.isEmpty(mimeType)) {
                            builder.setMimeType(mimeType);
                        }
                        final MetadataChangeSet changeSet = builder.build();
                        final String trackingTag = UUID.randomUUID().toString();
                        final ExecutionOptions executionOptions = new ExecutionOptions.Builder().setNotifyOnCompletion(true).setTrackingTag(trackingTag).build();

                        return RxDriveTask.toSingle(driveResourceClient.createFile(folder, changeSet, driveContents, executionOptions))
                                .flatMap(driveFile -> {
                                    final DriveId driveFileId = driveFile.getDriveId();
                                    if (driveFileId.getResourceId() == null) {
                                        return Single.create(emitter -> {
                                            final DriveIdUploadMetadata uploadMetadata = new DriveIdUploadMetadata(driveFileId, trackingTag);
                                            driveUploadCompleteManager.registerCallback(uploadMetadata, new DriveIdUploadCompleteCallback() {
                                                @Override
                                                public void onSuccess(@NonNull DriveId fetchedDriveId) {
                                                    if (!emitter.isDisposed()) {
                                                        emitter.onSuccess(fetchedDriveId.asDriveFile());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(@NonNull DriveId driveId) {
                                                    if (!emitter.isDisposed()) {
                                                        emitter.onError(new IOException("Failed to receive a Drive Id"));
                                                    }
                                                }
                                            });
                                        });
                                    } else {
                                        return Single.just(driveFile);
                                    }
                                });
                    } catch (IOException e) {
                        Logger.error(DriveDataStreams.this, "Failed write file with exception: ", e);
                        return RxDriveTask.toCompletable(driveResourceClient.discardContents(driveContents))
                                .andThen(Single.error(e));
                    } finally {
                        StorageManager.closeQuietly(fileInputStream);
                        StorageManager.closeQuietly(outputStream);
                    }
                });
    }

    @NonNull
    public synchronized Single<DriveFile> updateFile(@NonNull final Identifier driveIdentifier, @NonNull final File file) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveIdentifier);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(file);

        return getDriveId(driveIdentifier)
                .flatMap(driveId -> {
                    final DriveFile driveFile = driveId.asDriveFile();
                    return RxDriveTask.toSingle(driveResourceClient.openFile(driveFile, DriveFile.MODE_WRITE_ONLY))
                            .flatMap(driveContents -> {
                                OutputStream outputStream = null;
                                FileInputStream fileInputStream = null;
                                try {
                                    outputStream = driveContents.getOutputStream();
                                    fileInputStream = new FileInputStream(file);
                                    final byte[] buffer = new byte[BYTE_BUFFER_SIZE];

                                    int read;
                                    while ((read = fileInputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, read);
                                    }

                                    return RxDriveTask.toCompletable(driveResourceClient.commitContents(driveContents, null))
                                            .andThen(Single.just(driveFile));
                                } catch (IOException e) {
                                    Logger.error(DriveDataStreams.this, "Failed to update file with exception: ", e);
                                    return RxDriveTask.toCompletable(driveResourceClient.discardContents(driveContents))
                                            .andThen(Single.error(e));
                                } finally {
                                    StorageManager.closeQuietly(fileInputStream);
                                    StorageManager.closeQuietly(outputStream);
                                }
                            });
                });
    }

    @NonNull
    public synchronized Single<Boolean> delete(@NonNull final Identifier driveIdentifier) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveIdentifier);

        final Identifier smartReceiptsFolderId;
        if (smartReceiptsFolderSubject != null && smartReceiptsFolderSubject.getValue() != null && smartReceiptsFolderSubject.getValue().getDriveId().getResourceId() != null) {
            smartReceiptsFolderId = new Identifier(smartReceiptsFolderSubject.getValue().getDriveId().getResourceId());
        } else {
            smartReceiptsFolderId = null;
        }
        if (driveIdentifier.equals(smartReceiptsFolderId)) {
            Logger.info(DriveDataStreams.this, "Attempting to delete our Smart Receipts folder. Clearing our cached replay result...");
            smartReceiptsFolderSubject = null;
        }

        // Note: (https://developers.google.com/drive/android/trash) If the target of the trash/untrash operation is a folder, all descendants of that folder are similarly trashed or untrashed
        return getDriveId(driveIdentifier)
                .map(DriveId::asDriveResource)
                .flatMap(driveResource -> {
                    //noinspection ResultOfMethodCallIgnored
                    return RxDriveTask.toCompletable(driveResourceClient.delete(driveResource))
                            .andThen(Single.just(true))
                            .doOnSuccess(ignore -> Logger.info(DriveDataStreams.this, "Successfully deleted resource with status"))
                            .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to delete file with id: {}", driveIdentifier));
                });
    }

    public synchronized void clear() {
        Logger.info(DriveDataStreams.this, "Clearing our cached replay result...");
        smartReceiptsFolderSubject = null;
    }

    @NonNull
    public synchronized Single<File> download(@NonNull final DriveFile driveFile, @NonNull final File downloadLocationFile) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveFile);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(downloadLocationFile);

        return RxDriveTask.toSingle(driveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY))
                .flatMap(driveContents -> {
                    Logger.info(DriveDataStreams.this, "Successfully connected to the drive download stream");
                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        inputStream = driveContents.getInputStream();
                        fileOutputStream = new FileOutputStream(downloadLocationFile);
                        byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, read);
                        }

                        return RxDriveTask.toCompletable(driveResourceClient.discardContents(driveContents))
                                .andThen(Single.just(downloadLocationFile));

                    } catch (IOException e) {
                        Logger.error(DriveDataStreams.this, "Failed write file with exception: ", e);
                        return RxDriveTask.toCompletable(driveResourceClient.discardContents(driveContents))
                                .andThen(Single.error(e));
                    } finally {
                        StorageManager.closeQuietly(inputStream);
                        StorageManager.closeQuietly(fileOutputStream);
                    }
                });
    }

    private boolean isValidSmartReceiptsFolder(@NonNull Metadata metadata) {
        return metadata.isInAppFolder() && metadata.isFolder() && !metadata.isTrashed() && metadata.getDriveId().getResourceId() != null;
    }
}
