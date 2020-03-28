package co.smartreceipts.core.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Date;

import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.provider.SyncProvider;

public class DefaultSyncState implements SyncState {

    private final IdentifierMap mIdentifierMap;
    private final SyncStatusMap mSyncStatusMap;
    private final MarkedForDeletionMap mMarkedForDeletionMap;
    private final Date mLastLocalModificationTime;

    public DefaultSyncState() {
        this(new Date(System.currentTimeMillis()));
    }

    public DefaultSyncState(@NonNull Date lastLocalModificationTime) {
        this(null, null, null, lastLocalModificationTime);
    }

    public DefaultSyncState(@Nullable IdentifierMap identifierMap, @Nullable SyncStatusMap syncStatusMap, @Nullable MarkedForDeletionMap markedForDeletionMap, @Nullable Date lastLocalModificationTime) {
        mIdentifierMap = identifierMap;
        mSyncStatusMap = syncStatusMap;
        mMarkedForDeletionMap = markedForDeletionMap;
        mLastLocalModificationTime = lastLocalModificationTime;
    }

    private DefaultSyncState(@NonNull Parcel source) {
        mIdentifierMap = (IdentifierMap) source.readSerializable();
        mSyncStatusMap = (SyncStatusMap) source.readSerializable();
        mMarkedForDeletionMap = (MarkedForDeletionMap) source.readSerializable();
        mLastLocalModificationTime = new Date(source.readLong());
    }

    @Nullable
    @Override
    public Identifier getSyncId(@NonNull SyncProvider provider) {
        if (mIdentifierMap != null) {
            return mIdentifierMap.getSyncId(provider);
        } else {
            return null;
        }
    }

    @Override
    public boolean isSynced(@NonNull SyncProvider provider) {
        if (mSyncStatusMap != null) {
            return mSyncStatusMap.isSynced(provider);
        } else {
            return false;
        }
    }

    @Override
    public boolean isMarkedForDeletion(@NonNull SyncProvider provider) {
        if (mMarkedForDeletionMap != null) {
            return mMarkedForDeletionMap.isMarkedForDeletion(provider);
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public Date getLastLocalModificationTime() {
        if (mLastLocalModificationTime != null) {
            return mLastLocalModificationTime;
        } else {
            return new Date(0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultSyncState)) return false;

        DefaultSyncState syncState = (DefaultSyncState) o;

        if (mIdentifierMap != null ? !mIdentifierMap.equals(syncState.mIdentifierMap) : syncState.mIdentifierMap != null)
            return false;
        if (mSyncStatusMap != null ? !mSyncStatusMap.equals(syncState.mSyncStatusMap) : syncState.mSyncStatusMap != null)
            return false;
        if (mMarkedForDeletionMap != null ? !mMarkedForDeletionMap.equals(syncState.mMarkedForDeletionMap) : syncState.mMarkedForDeletionMap != null)
            return false;
        return mLastLocalModificationTime != null ? mLastLocalModificationTime.equals(syncState.mLastLocalModificationTime) : syncState.mLastLocalModificationTime == null;

    }

    @Override
    public int hashCode() {
        int result = mIdentifierMap != null ? mIdentifierMap.hashCode() : 0;
        result = 31 * result + (mSyncStatusMap != null ? mSyncStatusMap.hashCode() : 0);
        result = 31 * result + (mMarkedForDeletionMap != null ? mMarkedForDeletionMap.hashCode() : 0);
        result = 31 * result + (mLastLocalModificationTime != null ? mLastLocalModificationTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultSyncState{" +
                "mIdentifierMap=" + mIdentifierMap +
                ", mSyncStatusMap=" + mSyncStatusMap +
                ", mMarkedForDeletionMap=" + mMarkedForDeletionMap +
                ", mLastLocalModificationTime=" + mLastLocalModificationTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mIdentifierMap);
        dest.writeSerializable(mSyncStatusMap);
        dest.writeSerializable(mMarkedForDeletionMap);
        dest.writeLong(getLastLocalModificationTime().getTime());
    }

    public static final Parcelable.Creator<DefaultSyncState> CREATOR = new Parcelable.Creator<DefaultSyncState>() {
        public DefaultSyncState createFromParcel(Parcel source) {
            return new DefaultSyncState(source);
        }

        public DefaultSyncState[] newArray(int size) {
            return new DefaultSyncState[size];
        }
    };

}
