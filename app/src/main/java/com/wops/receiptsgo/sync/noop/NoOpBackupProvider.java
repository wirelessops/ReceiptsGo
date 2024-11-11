package com.wops.receiptsgo.sync.noop;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.sync.BackupProvider;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.errors.CriticalSyncError;
import com.wops.core.sync.errors.SyncErrorType;
import com.wops.core.sync.model.RemoteBackupMetadata;
import com.wops.core.sync.model.impl.Identifier;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A no-op implementation of the {@link BackupProvider} contract to help us to avoid dealing with nulls
 * in our {@link BackupProvidersManager} class
 */
@ApplicationScope
public class NoOpBackupProvider implements BackupProvider {

    @Inject
    public NoOpBackupProvider() {

    }

    @Override
    public void initialize(@NonNull FragmentActivity activity) {

    }

    @Override
    public void deinitialize() {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return false;
    }

    @NonNull
    @Override
    public Single<List<RemoteBackupMetadata>> getRemoteBackups() {
        return Single.just(Collections.emptyList());
    }

    @Nullable
    @Override
    public Identifier getDeviceSyncId() {
        return null;
    }

    @NonNull
    @Override
    public Date getLastDatabaseSyncTime() {
        return new Date(0L);
    }

    @NonNull
    @Override
    public Single<Boolean> restoreBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwriteExistingData) {
        return Single.just(false);
    }

    @NonNull
    @Override
    public Single<com.google.api.services.drive.model.File> renameBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull String newFileName) {
        return Single.just(new com.google.api.services.drive.model.File());
    }

    @NonNull
    @Override
    public Single<Boolean> deleteBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        return Single.just(false);
    }

    @Override
    public Single<Boolean> clearCurrentBackupConfiguration() {
        return Single.just(false);
    }

    @NonNull
    @Override
    public Single<List<File>> downloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return Single.just(Collections.emptyList());
    }

    @NonNull
    @Override
    public Single<List<File>> debugDownloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return Single.just(Collections.emptyList());
    }

    @NonNull
    @Override
    public Observable<CriticalSyncError> getCriticalSyncErrorStream() {
        return Observable.empty();
    }

    @Override
    public void markErrorResolved(@NonNull SyncErrorType syncErrorType) {
        // No-op
    }
}
