package co.smartreceipts.android.sync.provider;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Provider;

import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.GoogleDriveBackupManager;
import co.smartreceipts.android.sync.noop.NoOpBackupProvider;

public class SyncProviderFactory {

    @Inject
    Provider<GoogleDriveBackupManager> googleDriveBackupManagerProvider;

    @Inject
    public SyncProviderFactory() {
    }

    public BackupProvider get(@NonNull SyncProvider syncProvider) {
        if (syncProvider == SyncProvider.GoogleDrive) {
            return googleDriveBackupManagerProvider.get();
        } else if (syncProvider == SyncProvider.None) {
            return new NoOpBackupProvider();
        } else {
            throw new IllegalArgumentException("Unsupported sync provider type was specified");
        }
    }
}
