package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.common.base.Preconditions;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.DeviceMetadata;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
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

    /**
     * Saves the randomly generated UDID that is associated with this device. We leverage this in order to determine
     * if this is a "new" install (even on the same device) or is an existing sync for this device.
     */
    private static final CustomPropertyKey SMART_RECEIPTS_FOLDER_KEY = new CustomPropertyKey("smart_receipts_id", CustomPropertyKey.PUBLIC);

    private final GoogleApiClient mGoogleApiClient;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final Context mContext;
    private final DeviceMetadata mDeviceMetadata;
    private final DriveUploadCompleteManager mDriveUploadCompleteManager;
    private final Executor mExecutor;
    private ReplaySubject<DriveFolder> mSmartReceiptsFolderSubject;

    public DriveDataStreams(@NonNull Context context, @NonNull GoogleApiClient googleApiClient,
                            @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                            @NonNull DriveUploadCompleteManager driveUploadCompleteManager) {
        this(googleApiClient, context, googleDriveSyncMetadata, new DeviceMetadata(context), driveUploadCompleteManager, Executors.newCachedThreadPool());
    }

    public DriveDataStreams(@NonNull GoogleApiClient googleApiClient, @NonNull Context context, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                            @NonNull DeviceMetadata deviceMetadata, @NonNull DriveUploadCompleteManager driveUploadCompleteManager, @NonNull Executor executor) {
        mGoogleApiClient = Preconditions.checkNotNull(googleApiClient);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mGoogleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        mDeviceMetadata = Preconditions.checkNotNull(deviceMetadata);
        mDriveUploadCompleteManager = Preconditions.checkNotNull(driveUploadCompleteManager);
        mExecutor = Preconditions.checkNotNull(executor);
    }

    public synchronized Single<List<RemoteBackupMetadata>> getSmartReceiptsFolders() {
        return Single.create(emitter -> {
            final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, SMART_RECEIPTS_FOLDER)).build();
            Drive.DriveApi.query(mGoogleApiClient, folderQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                @Override
                public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    try {
                        final List<Metadata> folderMetadataList = new ArrayList<>();
                        for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            if (isValidSmartReceiptsFolder(metadata)) {
                                Logger.info(DriveDataStreams.this, "Tentatively found a Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                                folderMetadataList.add(metadata);
                            } else {
                                Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                            }
                        }

                        final AtomicInteger resultsCount = new AtomicInteger(folderMetadataList.size());
                        final List<RemoteBackupMetadata> resultsList = new ArrayList<>();
                        if (resultsCount.get() == 0) {
                            emitter.onSuccess(resultsList);
                        } else {
                            final Query databaseQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, DatabaseHelper.DATABASE_NAME)).build();
                            for (final Metadata metadata : folderMetadataList) {
                                final Identifier driveFolderId = new Identifier(metadata.getDriveId().getResourceId());
                                final Map<CustomPropertyKey, String> customPropertyMap = metadata.getCustomProperties();
                                final Identifier syncDeviceIdentifier;
                                if (customPropertyMap != null && customPropertyMap.containsKey(SMART_RECEIPTS_FOLDER_KEY)) {
                                    syncDeviceIdentifier = new Identifier(customPropertyMap.get(SMART_RECEIPTS_FOLDER_KEY));
                                    Logger.info(DriveDataStreams.this, "Found valid Smart Receipts folder a known device id");
                                } else {
                                    syncDeviceIdentifier = new Identifier("UnknownDevice");
                                    Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder without a tagged device key");
                                }

                                Logger.debug(DriveDataStreams.this, "Found existing Smart Receipts folder with id: {}", driveFolderId);
                                final String deviceName = metadata.getDescription() != null ? metadata.getDescription() : "";
                                final Date parentFolderLastModifiedDate = metadata.getModifiedDate();
                                metadata.getDriveId().asDriveFolder().queryChildren(mGoogleApiClient, databaseQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                                    @Override
                                    public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                                        try {
                                            Date lastModifiedDate = parentFolderLastModifiedDate;
                                            for (final Metadata databaseMetadata : metadataBufferResult.getMetadataBuffer()) {
                                                if (databaseMetadata.getModifiedDate().getTime() > lastModifiedDate.getTime()) {
                                                    lastModifiedDate = databaseMetadata.getModifiedDate();
                                                }
                                            }
                                            resultsList.add(new DefaultRemoteBackupMetadata(driveFolderId, syncDeviceIdentifier, deviceName, lastModifiedDate));
                                            Logger.debug(DriveDataStreams.this, "Successfully queried the backup metadata for the Smart Receipts folder with id: {}", driveFolderId);
                                        } finally {
                                            metadataBufferResult.getMetadataBuffer().release();
                                            if (resultsCount.decrementAndGet() == 0) {
                                                emitter.onSuccess(resultsList);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Status status) {
                                        Logger.error(DriveDataStreams.this, "Failed to query a database within the parent folder: {}", status);
                                        emitter.onError(new IOException(status.getStatusMessage()));
                                    }
                                });
                            }
                        }
                    } finally {
                        metadataBufferResult.getMetadataBuffer().release();
                    }
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to query a Smart Receipts folder with status: " + status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });
        });
    }

    public synchronized Observable<DriveFolder> getSmartReceiptsFolder() {
        if (mSmartReceiptsFolderSubject == null) {
            Logger.info(this, "Creating new replay subject for the Smart Receipts folder");
            mSmartReceiptsFolderSubject = ReplaySubject.create();

            Single.<DriveFolder>create(emitter -> {
                final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SMART_RECEIPTS_FOLDER_KEY, mGoogleDriveSyncMetadata.getDeviceIdentifier().getId())).build();
                Drive.DriveApi.query(mGoogleApiClient, folderQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        try {
                            DriveId folderId = null;
                            for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                                if (isValidSmartReceiptsFolder(metadata)) {
                                    folderId = metadata.getDriveId();
                                    break;
                                } else {
                                    Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", metadata.getDriveId().getResourceId());
                                }
                            }

                            if (folderId != null) {
                                Logger.info(DriveDataStreams.this, "Found an existing Google Drive folder for Smart Receipts");
                                emitter.onSuccess(folderId.asDriveFolder());
                            } else {
                                Logger.info(DriveDataStreams.this, "Failed to find an existing Smart Receipts folder for this device. Creating a new one...");
                                final MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(SMART_RECEIPTS_FOLDER).setDescription(mDeviceMetadata.getDeviceName()).setCustomProperty(SMART_RECEIPTS_FOLDER_KEY, mGoogleDriveSyncMetadata.getDeviceIdentifier().getId()).build();
                                Drive.DriveApi.getAppFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallbacks<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onSuccess(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                                        emitter.onSuccess(driveFolderResult.getDriveFolder());
                                    }

                                    @Override
                                    public void onFailure(@NonNull Status status) {
                                        Logger.error(DriveDataStreams.this, "Failed to create a home folder with status: {}", status);
                                        emitter.onError(new IOException(status.getStatusMessage()));
                                    }
                                });
                            }
                        } finally {
                            metadataBufferResult.getMetadataBuffer().release();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {
                        Logger.error(DriveDataStreams.this, "Failed to query a Smart Receipts folder with status: {}", status);
                        emitter.onError(new IOException(status.getStatusMessage()));
                    }
                });
            })
                    .toObservable()
                    .subscribe(mSmartReceiptsFolderSubject);
        }
        return mSmartReceiptsFolderSubject;
    }

    @NonNull
    public synchronized Single<DriveId> getDriveId(@NonNull final Identifier identifier) {
        Preconditions.checkNotNull(identifier);

        return Single.create(emitter -> Drive.DriveApi.fetchDriveId(mGoogleApiClient, identifier.getId()).setResultCallback(new ResultCallbacks<DriveApi.DriveIdResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.DriveIdResult driveIdResult) {
                final DriveId driveId = driveIdResult.getDriveId();
                Logger.debug(DriveDataStreams.this, "Successfully fetch file with id: {}", driveId);
                emitter.onSuccess(driveId);
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Logger.error(DriveDataStreams.this, "Failed to fetch {} with status: {}", identifier, status);
                emitter.onError(new IOException(status.getStatusMessage()));
            }
        }));
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder) {
        Preconditions.checkNotNull(driveFolder);

        return Observable.create(emitter -> {
            final Query folderQuery = new Query.Builder().build();
            driveFolder.queryChildren(mGoogleApiClient, folderQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                @Override
                public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    try {
                        for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            if (!metadata.isTrashed()) {
                                emitter.onNext(metadata.getDriveId());
                            }
                        }
                        emitter.onComplete();
                    } finally {
                        metadataBufferResult.getMetadataBuffer().release();
                    }
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to query files in folder with status: {}", status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });

        });
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder, @NonNull final String fileName) {
        Preconditions.checkNotNull(driveFolder);
        Preconditions.checkNotNull(fileName);

        return Observable.create(emitter -> {
            final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, fileName)).build();
            driveFolder.queryChildren(mGoogleApiClient, folderQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                @Override
                public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    try {
                        for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            if (!metadata.isTrashed()) {
                                emitter.onNext(metadata.getDriveId());
                            }
                        }
                        emitter.onComplete();
                    } finally {
                        metadataBufferResult.getMetadataBuffer().release();
                    }
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to query files in folder with status: {}", status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });

        });
    }

    @NonNull
    public synchronized Single<Metadata> getMetadata(@NonNull final DriveFile driveFile) {
        Preconditions.checkNotNull(driveFile);

        return Single.create(emitter -> {
            driveFile.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallbacks<DriveResource.MetadataResult>() {
                @Override
                public void onSuccess(@NonNull DriveResource.MetadataResult metadataResult) {
                    emitter.onSuccess(metadataResult.getMetadata());
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to query files in folder with status: {}", status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });
        });
    }

    public synchronized Single<DriveFile> createFileInFolder(@NonNull final DriveFolder folder, @NonNull final File file) {
        Preconditions.checkNotNull(folder);
        Preconditions.checkNotNull(file);

        return Single.create(emitter -> {
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallbacks<DriveApi.DriveContentsResult>() {
                @Override
                public void onSuccess(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
                    mExecutor.execute(() -> {
                        final DriveContents driveContents = driveContentsResult.getDriveContents();
                        OutputStream outputStream = null;
                        FileInputStream fileInputStream = null;
                        try {
                            outputStream = driveContents.getOutputStream();
                            fileInputStream = new FileInputStream(file);
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = fileInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, read);
                            }

                            final Uri uri = Uri.fromFile(file);
                            final String mimeType = UriUtils.getMimeType(uri, mContext.getContentResolver());
                            final MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                            builder.setTitle(file.getName());
                            if (!TextUtils.isEmpty(mimeType)) {
                                builder.setMimeType(mimeType);
                            }
                            final MetadataChangeSet changeSet = builder.build();
                            final String trackingTag = UUID.randomUUID().toString();
                            folder.createFile(mGoogleApiClient, changeSet, driveContents, new ExecutionOptions.Builder().setNotifyOnCompletion(true).setTrackingTag(trackingTag).build()).setResultCallback(new ResultCallbacks<DriveFolder.DriveFileResult>() {
                                @Override
                                public void onSuccess(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                    final DriveFile driveFile = driveFileResult.getDriveFile();
                                    final DriveId driveFileId = driveFile.getDriveId();
                                    if (driveFileId.getResourceId() == null) {
                                        final DriveIdUploadMetadata uploadMetadata = new DriveIdUploadMetadata(driveFileId, trackingTag);
                                        mDriveUploadCompleteManager.registerCallback(uploadMetadata, new DriveIdUploadCompleteCallback() {
                                            @Override
                                            public void onSuccess(@NonNull DriveId fetchedDriveId) {
                                                emitter.onSuccess(fetchedDriveId.asDriveFile());
                                            }

                                            @Override
                                            public void onFailure(@NonNull DriveId driveId) {
                                                emitter.onError(new IOException("Failed to receive a Drive Id"));
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Status status) {
                                    Logger.error(DriveDataStreams.this, "Failed to create file with status: {}", status);
                                    emitter.onError(new IOException(status.getStatusMessage()));
                                }
                            });
                        } catch (IOException e) {
                            Logger.error(DriveDataStreams.this, "Failed write file with exception: ", e);
                            driveContents.discard(mGoogleApiClient);
                            emitter.onError(e);
                        } finally {
                            StorageManager.closeQuietly(fileInputStream);
                            StorageManager.closeQuietly(outputStream);
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to create file with status: " + status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });
        });
    }

    public synchronized Single<DriveFile> updateFile(@NonNull final Identifier driveIdentifier, @NonNull final File file) {
        Preconditions.checkNotNull(driveIdentifier);
        Preconditions.checkNotNull(file);

        return Single.create(emitter -> {
            Drive.DriveApi.fetchDriveId(mGoogleApiClient, driveIdentifier.getId()).setResultCallback(new ResultCallbacks<DriveApi.DriveIdResult>() {
                @Override
                public void onSuccess(@NonNull DriveApi.DriveIdResult driveIdResult) {
                    final DriveId driveId = driveIdResult.getDriveId();
                    final DriveFile driveFile = driveId.asDriveFile();
                    driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallbacks<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onSuccess(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
                            mExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    final DriveContents driveContents = driveContentsResult.getDriveContents();
                                    OutputStream outputStream = null;
                                    FileInputStream fileInputStream = null;
                                    try {
                                        outputStream = driveContents.getOutputStream();
                                        fileInputStream = new FileInputStream(file);
                                        byte[] buffer = new byte[8192];
                                        int read;
                                        while ((read = fileInputStream.read(buffer)) != -1) {
                                            outputStream.write(buffer, 0, read);
                                        }

                                        driveContents.commit(mGoogleApiClient, null).setResultCallback(new ResultCallbacks<Status>() {
                                            @Override
                                            public void onSuccess(@NonNull Status status) {
                                                emitter.onSuccess(driveFile);
                                            }

                                            @Override
                                            public void onFailure(@NonNull Status status) {
                                                Logger.error(DriveDataStreams.this, "Failed to updateDriveFile file with status: {}", status);
                                                emitter.onError(new IOException(status.getStatusMessage()));
                                            }
                                        });
                                    } catch (IOException e) {
                                        Logger.error(DriveDataStreams.this, "Failed write file with exception: ", e);
                                        driveContents.discard(mGoogleApiClient);
                                        emitter.onError(e);
                                    } finally {
                                        StorageManager.closeQuietly(fileInputStream);
                                        StorageManager.closeQuietly(outputStream);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Status status) {
                            Logger.error(DriveDataStreams.this, "Failed to updateDriveFile file with status: {}", status);
                            emitter.onError(new IOException(status.getStatusMessage()));
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveDataStreams.this, "Failed to fetch drive id {} to updateDriveFile with status: {}", driveIdentifier, status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });
        });
    }

    public synchronized Single<Boolean> delete(@NonNull final Identifier driveIdentifier) {
        Preconditions.checkNotNull(driveIdentifier);

        final Identifier smartReceiptsFolderId;
        if (mSmartReceiptsFolderSubject != null && mSmartReceiptsFolderSubject.getValue() != null && mSmartReceiptsFolderSubject.getValue().getDriveId().getResourceId() != null) {
            smartReceiptsFolderId = new Identifier(mSmartReceiptsFolderSubject.getValue().getDriveId().getResourceId());
        } else {
            smartReceiptsFolderId = null;
        }
        if (driveIdentifier.equals(smartReceiptsFolderId)) {
            Logger.info(DriveDataStreams.this, "Attemping to delete our Smart Receipts folder. Clearing our cached replay result...");
            mSmartReceiptsFolderSubject = null;
        }

        // Note: (https://developers.google.com/drive/android/trash) If the target of the trash/untrash operation is a folder, all descendants of that folder are similarly trashed or untrashed
        return Single.create(emitter -> Drive.DriveApi.fetchDriveId(mGoogleApiClient, driveIdentifier.getId()).setResultCallback(new ResultCallbacks<DriveApi.DriveIdResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.DriveIdResult driveIdResult) {
                final DriveId driveId = driveIdResult.getDriveId();
                final DriveResource driveResource = driveId.asDriveResource();
                driveResource.delete(mGoogleApiClient).setResultCallback(new ResultCallbacks<Status>() {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        Logger.info(DriveDataStreams.this, "Successfully deleted resource with status: {}", status);
                        emitter.onSuccess(true);
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {
                        Logger.error(DriveDataStreams.this, "Failed to delete resource with status: {}", status);
                        emitter.onSuccess(false);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Logger.error(DriveDataStreams.this, "Failed to fetch drive id " + driveIdentifier + " to deleteFolder with status: {}", status);
                emitter.onError(new IOException(status.getStatusMessage()));
            }
        }));
    }

    public synchronized void clear() {
        Logger.info(DriveDataStreams.this, "Clearing our cached replay result...");
        mSmartReceiptsFolderSubject = null;
    }

    public synchronized Single<File> download(@NonNull final DriveFile driveFile, @NonNull final File downloadLocationFile) {
        Preconditions.checkNotNull(driveFile);
        Preconditions.checkNotNull(downloadLocationFile);

        return Single.create(emitter -> driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallbacks<DriveApi.DriveContentsResult>() {
            @Override
            public void onSuccess(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
                mExecutor.execute(() -> {
                    Logger.info(DriveDataStreams.this, "Successfully connected to the drive download stream");
                    final DriveContents driveContents = driveContentsResult.getDriveContents();
                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        inputStream = driveContents.getInputStream();
                        fileOutputStream = new FileOutputStream(downloadLocationFile);
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, read);
                        }
                        driveContents.discard(mGoogleApiClient);
                        emitter.onSuccess(downloadLocationFile);
                    } catch (IOException e) {
                        Logger.error(DriveDataStreams.this, "Failed write file with exception: ", e);
                        driveContents.discard(mGoogleApiClient);
                        emitter.onError(e);
                    } finally {
                        StorageManager.closeQuietly(inputStream);
                        StorageManager.closeQuietly(fileOutputStream);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Logger.error(DriveDataStreams.this, "Failed to downloaded the drive resource with status: {}", status);
            }
        }));
    }

    private boolean isValidSmartReceiptsFolder(@NonNull Metadata metadata) {
        return metadata.isInAppFolder() && metadata.isFolder() && !metadata.isTrashed();
    }
}
