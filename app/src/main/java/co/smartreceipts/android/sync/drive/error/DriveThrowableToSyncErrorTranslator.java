package co.smartreceipts.android.sync.drive.error;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;

public class DriveThrowableToSyncErrorTranslator {

    private static final String NO_REMOTE_DISK_SPACE = "Failed to receive a Drive Id";
    private static final String USER_REVOKED_REMOTE_RIGHTS = "Authorization has been revoked by the user. Reconnect the Drive API client to reauthorize.";
    private static final String USER_DELETED_REMOTE_DATA = "Drive item not found, or you are not authorized to access it.";
    private static final String INVALID_PARENT_FOLDER = "Invalid parent folder.";

    @NonNull
    public Throwable get(@NonNull Throwable throwable) {
        final String message = Preconditions.checkNotNull(throwable).getMessage();

        if (NO_REMOTE_DISK_SPACE.equals(message)) {
            return new CriticalSyncError(throwable, SyncErrorType.NoRemoteDiskSpace);
        } else if (USER_REVOKED_REMOTE_RIGHTS.equals(message)) {
            return new CriticalSyncError(throwable, SyncErrorType.UserRevokedRemoteRights);
        } else if (USER_DELETED_REMOTE_DATA.equals(message)) {
            return new CriticalSyncError(throwable, SyncErrorType.DriveRecoveryRequired);
        } else if (INVALID_PARENT_FOLDER.equals(message)) {
            return new CriticalSyncError(throwable, SyncErrorType.DriveRecoveryRequired);
        } else {
            return throwable;
        }
    }
}
