package com.wops.receiptsgo.sync;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;

import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.sync.errors.CriticalSyncError;
import co.smartreceipts.core.sync.errors.SyncErrorType;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import com.wops.receiptsgo.sync.network.NetworkManager;
import com.wops.receiptsgo.sync.network.SupportedNetworkType;
import co.smartreceipts.core.sync.provider.SyncProvider;
import com.wops.receiptsgo.sync.provider.SyncProviderFactory;
import com.wops.receiptsgo.sync.provider.SyncProviderStore;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A global manager for whatever our current backup provider may (or may not) be
 */
@ApplicationScope
public class BackupProvidersManager implements BackupProvider {

    private final SyncProviderFactory syncProviderFactory;
    private final SyncProviderStore syncProviderStore;
    private final NetworkManager networkManager;

    private final Set<BackupProviderChangeListener> backupProviderChangeListeners = new CopyOnWriteArraySet<>();
    private BackupProvider backupProvider;

    @Inject
    public BackupProvidersManager(@NonNull SyncProviderFactory syncProviderFactory,
                                  @NonNull SyncProviderStore syncProviderStore,
                                  @NonNull NetworkManager networkManager) {
        this.syncProviderFactory = Preconditions.checkNotNull(syncProviderFactory);
        this.syncProviderStore = Preconditions.checkNotNull(syncProviderStore);
        this.networkManager = Preconditions.checkNotNull(networkManager);
        this.backupProvider = syncProviderFactory.get(this.syncProviderStore.getProvider());
    }

    public void registerChangeListener(@NonNull BackupProviderChangeListener backupProviderChangeListener) {
        Preconditions.checkNotNull(backupProviderChangeListener);
        backupProviderChangeListeners.add(backupProviderChangeListener);
    }

    public void unregisterChangeListener(@NonNull BackupProviderChangeListener backupProviderChangeListener) {
        Preconditions.checkNotNull(backupProviderChangeListener);
        backupProviderChangeListeners.remove(backupProviderChangeListener);
    }

    public synchronized void setAndInitializeSyncProvider(@NonNull SyncProvider syncProvider, @Nullable FragmentActivity fragmentActivity) {
        if (syncProviderStore.setSyncProvider(syncProvider)) {
            Logger.info(this, "Initializing new Backup Provider: {}", syncProvider);
            backupProvider.deinitialize();
            backupProvider = syncProviderFactory.get(syncProvider);
            backupProvider.initialize(fragmentActivity);
            for (final BackupProviderChangeListener backupProviderChangeListener : backupProviderChangeListeners) {
                backupProviderChangeListener.onProviderChanged(syncProvider);
            }
        }
    }

    public synchronized void setAndInitializeNetworkProviderType(@NonNull SupportedNetworkType supportedNetworkType) {
        networkManager.setAndInitializeNetworkProviderType(supportedNetworkType);
    }

    @NonNull
    public SyncProvider getSyncProvider() {
        return syncProviderStore.getProvider();
    }

    @Override
    public synchronized void initialize(@Nullable FragmentActivity activity) {
        backupProvider.initialize(activity);
    }

    @Override
    public synchronized void deinitialize() {
        backupProvider.deinitialize();
    }

    @Override
    public synchronized boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return backupProvider.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public Single<List<RemoteBackupMetadata>> getRemoteBackups() {
        return backupProvider.getRemoteBackups();
    }

    @Nullable
    @Override
    public Identifier getDeviceSyncId() {
        return backupProvider.getDeviceSyncId();
    }

    @NonNull
    @Override
    public Date getLastDatabaseSyncTime() {
        return backupProvider.getLastDatabaseSyncTime();
    }

    @NonNull
    @Override
    public Single<Boolean> restoreBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwriteExistingData) {
        return backupProvider.restoreBackup(remoteBackupMetadata, overwriteExistingData);
    }

    @NonNull
    @Override
    public Single<com.google.api.services.drive.model.File> renameBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull String newFileName) {
        return backupProvider.renameBackup(remoteBackupMetadata, newFileName);
    }

    @NonNull
    @Override
    public Single<Boolean> deleteBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        return backupProvider.deleteBackup(remoteBackupMetadata);
    }

    @Override
    public Single<Boolean> clearCurrentBackupConfiguration() {
        return backupProvider.clearCurrentBackupConfiguration();
    }

    @NonNull
    @Override
    public Single<List<File>> downloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return backupProvider.downloadAllData(remoteBackupMetadata, downloadLocation);
    }

    @NonNull
    @Override
    public Single<List<File>> debugDownloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return backupProvider.debugDownloadAllData(remoteBackupMetadata, downloadLocation);
    }

    @NonNull
    @Override
    public Observable<CriticalSyncError> getCriticalSyncErrorStream() {
        return backupProvider.getCriticalSyncErrorStream();
    }

    @Override
    public void markErrorResolved(@NonNull SyncErrorType syncErrorType) {
        backupProvider.markErrorResolved(syncErrorType);
    }
}
