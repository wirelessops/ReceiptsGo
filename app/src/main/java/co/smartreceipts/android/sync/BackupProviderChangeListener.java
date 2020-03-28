package co.smartreceipts.android.sync;

import androidx.annotation.NonNull;

import co.smartreceipts.core.sync.provider.SyncProvider;

public interface BackupProviderChangeListener {

    void onProviderChanged(@NonNull SyncProvider newProvider);
}
