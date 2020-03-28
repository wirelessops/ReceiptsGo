package co.smartreceipts.android.sync.provider;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.noop.NoOpBackupProvider;
import co.smartreceipts.core.sync.provider.SyncProvider;

public class SyncProviderFactory {

    public final static String DRIVE_BACKUP_MANAGER = "google_backup_provider";

    @Inject
    @Named(DRIVE_BACKUP_MANAGER)
    Provider<BackupProvider> googleDriveBackupManagerProvider;

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
