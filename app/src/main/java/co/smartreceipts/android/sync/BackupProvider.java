package co.smartreceipts.android.sync;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.sql.Date;
import java.util.List;

import co.smartreceipts.core.sync.errors.CriticalSyncError;
import co.smartreceipts.core.sync.errors.SyncErrorType;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A top level interface to track the core behaviors that are shared by all automatic backup providers
 */
public interface BackupProvider {

    /**
     * Initialize the backup provider
     *
     * @param activity the current {@link FragmentActivity} if one is required for connection error resolutions
     */
    void initialize(@NonNull FragmentActivity activity);

    /**
     * De-initialize the backup provider to stop it from being used
     */
    void deinitialize();

    /**
     * Passes an activity result along to this provider for processing if required
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data any intent data
     * @return {@code true} if we handle the request, {@code false} otherwise
     */
    boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    /**
     * @return an {@link Single} containing all of our remote backups
     */
    @NonNull
    Single<List<RemoteBackupMetadata>> getRemoteBackups();

    /**
     * @return the sync {@link Identifier} for the current device or {@code null} if none is defined
     */
    @Nullable
    Identifier getDeviceSyncId();

    /**
     * @return the date for the last time our database was synced
     */
    @NonNull
    Date getLastDatabaseSyncTime();

    /**
     * Restores an existing backup
     *
     * @param remoteBackupMetadata the metadata to restore
     * @param overwriteExistingData if we should overwrite the existing data
     * @return a {@link Single} for the restore operation with a success boolean
     */
    @NonNull
    Single<Boolean> restoreBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwriteExistingData);

    /**
     * Deletes an existing backup
     *
     * @param remoteBackupMetadata the metadata to delete
     * @return an {@link Single} for the delete operation with a success boolean
     */
    @NonNull
    Single<Boolean> deleteBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata);


    /**
     * Attempts to clear out the current backup configuration
     *
     * @return an {@link Single} for the delete operation with a success boolean
     */
    Single<Boolean> clearCurrentBackupConfiguration();

    /**
     * Downloads an existing backup to a specific location
     *
     * @param remoteBackupMetadata the metadata to download
     * @param downloadLocation the {@link File} location to download it to
     * @return a {@link Single} that contains the downloaded images
     */
    @NonNull
    Single<List<File>> downloadAllData(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation);

    /**
     * Downloads an existing backup to a specific location in a debug friendly manner
     *
     * @param remoteBackupMetadata the metadata to download
     * @param downloadLocation the {@link File} location to download it to
     * @return an {@link Single} that contains the downloaded images
     */
    @NonNull
    Single<List<File>> debugDownloadAllData(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation);

    /**
     * @return an {@link Observable} that emits {@link CriticalSyncError} instances whenever they occur,
     * allowing us to respond as appropriately for these items
     */
    @NonNull
    Observable<CriticalSyncError> getCriticalSyncErrorStream();

    /**
     * Call this method to mark this particular error as resolve
     *
     * @param syncErrorType the {@link SyncErrorType} to mark as resolved
     */
    void markErrorResolved(@NonNull SyncErrorType syncErrorType);


}
