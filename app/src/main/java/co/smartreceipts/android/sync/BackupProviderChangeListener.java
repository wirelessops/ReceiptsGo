package co.smartreceipts.android.sync;

import androidx.annotation.NonNull;

import co.smartreceipts.android.sync.provider.SyncProvider;

public interface BackupProviderChangeListener {

    void onProviderChanged(@NonNull SyncProvider newProvider);
}
