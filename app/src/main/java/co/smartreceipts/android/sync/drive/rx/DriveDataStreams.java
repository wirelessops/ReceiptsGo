package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.sync.drive.DriveServiceHelper;
import co.smartreceipts.android.sync.drive.device.DeviceMetadata;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.debug.DriveFilesAndFoldersPrinter;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.DefaultRemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

class DriveDataStreams {

    private static final String SMART_RECEIPTS_FOLDER = "Smart Receipts";

    /**
     * Saves the randomly generated UDID that is associated with this device. We leverage this in order to determine
     * if this is a "new" install (even on the same device) or is an existing sync for this device.
     */
    private static final String SMART_RECEIPTS_FOLDER_KEY = "smart_receipts_id";
    private static final String FOLDER_NAME_QUERY = "name = 'Smart Receipts'";
    private static final String FOLDER_PARENTS_QUERY = "' in parents and name = 'receipts.db'";
    private static final String APP_DATA_FOLDER_NAME = "appDataFolder";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private static final String PROPERTIES_QUERY = "properties has { key='smart_receipts_id' and value='";

    private final DriveServiceHelper driveServiceHelper;
    private final GoogleDriveSyncMetadata googleDriveSyncMetadata;
    private final DeviceMetadata deviceMetadata;

    private ReplaySubject<File> smartReceiptsFolderSubject;

    public DriveDataStreams(@NonNull Context context,
                            @NonNull DriveServiceHelper driveServiceHelper,
                            @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata) {
        this(driveServiceHelper, googleDriveSyncMetadata, new DeviceMetadata(context));
    }

    public DriveDataStreams(@NonNull DriveServiceHelper driveServiceHelper,
                            @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                            @NonNull DeviceMetadata deviceMetadata) {
        this.driveServiceHelper = Preconditions.checkNotNull(driveServiceHelper);
        this.googleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        this.deviceMetadata = Preconditions.checkNotNull(deviceMetadata);
    }

    @NonNull
    public synchronized Single<List<RemoteBackupMetadata>> getSmartReceiptsFolders() {
        return driveServiceHelper.querySingle(FOLDER_NAME_QUERY)
                .flatMap(fileList -> {
                    final List<File> folderFileList = new ArrayList<>();
                    for (final File file : fileList.getFiles()) {
                        if (isValidSmartReceiptsFolder(file)) {
                            Logger.info(DriveDataStreams.this, "Tentatively found a Smart Receipts folder during metadata pre-check: {}", file.getId());
                            folderFileList.add(file);
                        } else {
                            Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", file.getId());
                        }
                    }
                    return Single.just(folderFileList);
                })
                .flatMap(files -> Observable.fromIterable(files)
                        .flatMap(file -> {
                            String fileId = file.getId();
                            String queryStr = "'".concat(fileId).concat(FOLDER_PARENTS_QUERY);
                            return driveServiceHelper.queryObservable(queryStr)
                                    .map(databaseFileList -> {
                                        // Get the folder resource id
                                        final String validResourceId = file.getId();
                                        final Identifier driveFolderId = new Identifier(validResourceId);
                                        final Map<String, String> customPropertyMap = file.getProperties();
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
                                        final String deviceName = file.getDescription() != null ? file.getDescription() : "";

                                        // Set the last modified date, using the last database update
                                        DateTime lastModifiedDate = file.getModifiedTime();
                                        for (final File f : databaseFileList.getFiles()) {
                                            if (f.getModifiedTime().getValue() > lastModifiedDate.getValue()) {
                                                lastModifiedDate = f.getModifiedTime();
                                            }
                                        }

                                        // Return all of this via our internal database wrapper
                                        return new DefaultRemoteBackupMetadata(driveFolderId, syncDeviceIdentifier, deviceName, lastModifiedDate);
                                    })
                                    .doOnNext(backupMetadata -> Logger.debug(DriveDataStreams.this, "Successfully queried the backup metadata for the Smart Receipts folder with id: {}", backupMetadata.getId()))
                                    .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query a database within the parent folder: {}", fileId));
                        })
                        .toList()
                        .flatMap(defaultRemoteBackupMetadataList -> {
                            // Note: We create this new list to cast to the interface variant of the list
                            return Single.just(new ArrayList<>(defaultRemoteBackupMetadataList));
                        }));
    }

    @NonNull
    public synchronized Observable<File> getSmartReceiptsFolder() {
        if (smartReceiptsFolderSubject == null) {
            Logger.info(this, "Creating new replay subject for the Smart Receipts folder");
            smartReceiptsFolderSubject = ReplaySubject.create();
            driveServiceHelper.querySingle(PROPERTIES_QUERY
                    .concat(googleDriveSyncMetadata.getDeviceIdentifier().getId()).concat("' }"))
                    .map(fileList -> {
                        File fileId = null;
                        for (final File file : fileList.getFiles()) {
                            if (isValidSmartReceiptsFolder(file)) {
                                fileId = file;
                                break;
                            } else {
                                Logger.warn(DriveDataStreams.this, "Found an invalid Smart Receipts folder during metadata pre-check: {}", file.getId());
                            }
                        }
                        return Optional.ofNullable(fileId);
                    })
                    .flatMap(driveIdOptional -> {
                        if (driveIdOptional.isPresent()) {
                            Logger.info(DriveDataStreams.this, "Found an existing Google Drive folder for Smart Receipts");
                            return Single.just(driveIdOptional.get());
                        } else {
                            Logger.info(DriveDataStreams.this, "Failed to find an existing Smart Receipts folder for this device. Creating a new one...");
                            Map<String, String> properties = new HashMap<>();
                            properties.put(SMART_RECEIPTS_FOLDER_KEY, googleDriveSyncMetadata.getDeviceIdentifier().getId());
                            return driveServiceHelper.createFile(SMART_RECEIPTS_FOLDER, FOLDER_MIME_TYPE, deviceMetadata.getDeviceName(), properties, APP_DATA_FOLDER_NAME, null)
                                    .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to create a home folder with error: {}", throwable.getMessage()));
                        }
                    })
                    .toObservable()
                    .subscribe(smartReceiptsFolderSubject);
        }
        return smartReceiptsFolderSubject;
    }

    @NonNull
    public synchronized Single<FileList> getAllFiles() {
        return driveServiceHelper.getAllFilesSortedByTime()
                .doOnError(throwable -> Logger.error(DriveFilesAndFoldersPrinter.class, "Failed to query with status: {}" , throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<FileList> getFilesInFolder(@NonNull final String folderId) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(folderId);

        return driveServiceHelper.getFilesInFolder(folderId)
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query files in folder with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<FileList> getFilesInFolder(@NonNull final String folderId, @NonNull final String fileName) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(folderId);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(fileName);

        return driveServiceHelper.getFilesByNameInFolder(folderId, fileName)
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to query files in folder by name with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<File> getMetadata(@NonNull final String fileId) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(fileId);

        return driveServiceHelper.getFile(fileId)
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to get metadata for file with status: {}", throwable.getMessage()));
    }

    @NonNull
    public synchronized Single<File> createFileInFolder(@NonNull final File folder, @NonNull final java.io.File file) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(folder);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(file);

        return driveServiceHelper.createFile(file.getName(), null, null, null, folder.getId(), file);
    }

    @NonNull
    public synchronized Single<File> updateFile(@NonNull final Identifier driveIdentifier, @NonNull final java.io.File file) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveIdentifier);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(file);

        String fileId = driveIdentifier.getId();
        return driveServiceHelper.updateFile(fileId, file);
    }

    @NonNull
    public synchronized Single<Boolean> delete(@NonNull final Identifier driveIdentifier) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(driveIdentifier);

        final Identifier smartReceiptsFolderId;
        if (smartReceiptsFolderSubject != null && smartReceiptsFolderSubject.getValue() != null && smartReceiptsFolderSubject.getValue().getId() != null) {
            smartReceiptsFolderId = new Identifier(smartReceiptsFolderSubject.getValue().getId());
        } else {
            smartReceiptsFolderId = null;
        }
        if (driveIdentifier.equals(smartReceiptsFolderId)) {
            Logger.info(DriveDataStreams.this, "Attempting to delete our Smart Receipts folder. Clearing our cached replay result...");
            smartReceiptsFolderSubject = null;
        }

        // Note: (https://developers.google.com/drive/android/trash) If the target of the trash/untrash operation is a folder, all descendants of that folder are similarly trashed or untrashed
        return driveServiceHelper.deleteFile(driveIdentifier.getId())
                .andThen(Single.just(true))
                .doOnSuccess(ignore -> Logger.info(DriveDataStreams.this, "Successfully deleted resource with status"))
                .doOnError(throwable -> Logger.error(DriveDataStreams.this, "Failed to delete file with id: {}", driveIdentifier));
    }

    public synchronized void clear() {
        Logger.info(DriveDataStreams.this, "Clearing our cached replay result...");
        smartReceiptsFolderSubject = null;
    }

    @NonNull
    public synchronized Single<java.io.File> download(@NonNull final String fileId, @NonNull final java.io.File downloadLocationFile) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(fileId);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(downloadLocationFile);

        return driveServiceHelper.getDriveFileAsJavaFile(fileId, downloadLocationFile);
    }

    private boolean isValidSmartReceiptsFolder(@NonNull File file) {
        return file.getMimeType().equals(FOLDER_MIME_TYPE) && !file.getTrashed() && file.getId() != null;
    }
}
