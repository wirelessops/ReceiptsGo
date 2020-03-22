package co.smartreceipts.core.sync.errors;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class CriticalSyncError extends Exception {

    private final SyncErrorType mSyncErrorType;

    public CriticalSyncError(@NonNull Throwable cause, @NonNull SyncErrorType syncErrorType) {
        super(Preconditions.checkNotNull(cause));
        mSyncErrorType = Preconditions.checkNotNull(syncErrorType);
    }

    @NonNull
    public SyncErrorType getSyncErrorType() {
        return mSyncErrorType;
    }
}
