package co.smartreceipts.android.sync.noop;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
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
        return Single.just(Collections.<RemoteBackupMetadata>emptyList());
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
        return Single.just(Collections.<File>emptyList());
    }

    @NonNull
    @Override
    public Single<List<File>> debugDownloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return Single.just(Collections.<File>emptyList());
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
