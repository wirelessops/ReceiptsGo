package co.smartreceipts.core.sync.model.impl;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import co.smartreceipts.core.sync.provider.SyncProvider;

public class SyncStatusMap implements Serializable {

    @SerializedName("sync_status_map")
    private final Map<SyncProvider, Boolean> mSyncStatusMap;

    public SyncStatusMap(@NonNull Map<SyncProvider, Boolean> syncStatusMap) {
        mSyncStatusMap = new HashMap<>(Preconditions.checkNotNull(syncStatusMap));
    }

    public boolean isSynced(@NonNull SyncProvider syncProvider) {
        if (mSyncStatusMap.containsKey(syncProvider)) {
            return mSyncStatusMap.get(syncProvider);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncStatusMap)) return false;

        SyncStatusMap that = (SyncStatusMap) o;

        return mSyncStatusMap != null ? mSyncStatusMap.equals(that.mSyncStatusMap) : that.mSyncStatusMap == null;

    }

    @Override
    public int hashCode() {
        return mSyncStatusMap != null ? mSyncStatusMap.hashCode() : 0;
    }
}
