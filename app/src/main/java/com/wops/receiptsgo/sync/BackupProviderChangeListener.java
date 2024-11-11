package com.wops.receiptsgo.sync;

import androidx.annotation.NonNull;

import com.wops.core.sync.provider.SyncProvider;

public interface BackupProviderChangeListener {

    void onProviderChanged(@NonNull SyncProvider newProvider);
}
