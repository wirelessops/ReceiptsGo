package co.smartreceipts.android.sync.drive.rx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.drive.model.File;
import com.google.common.base.Preconditions;

import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.android.sync.model.impl.SyncStatusMap;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class DriveStreamMappings {

    @NonNull
    public SyncState postInsertSyncState(@NonNull SyncState oldSyncState, @Nullable File driveFile) {
        Preconditions.checkNotNull(oldSyncState);

        if (driveFile != null) {
            return new DefaultSyncState(getSyncIdentifierMap(driveFile), newDriveSyncedStatusMap(),
                    new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), new Date(System.currentTimeMillis()));
        } else {
            return new DefaultSyncState(null, newDriveSyncedStatusMap(),
                    new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), new Date(System.currentTimeMillis()));
        }
    }

    @NonNull
    public SyncState postUpdateSyncState(@NonNull SyncState oldSyncState, @NonNull File driveFile) {
        return new DefaultSyncState(getSyncIdentifierMap(driveFile), newDriveSyncedStatusMap(),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), new Date(System.currentTimeMillis()));
    }

    @NonNull
    public SyncState postDeleteSyncState(@NonNull SyncState oldSyncState, boolean isFullDelete) {
        final MarkedForDeletionMap markedForDeletionMap;
        if (isFullDelete) {
            markedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true));
        } else {
            markedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, oldSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive)));
        }
        return new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, null)),
                newDriveSyncedStatusMap(), markedForDeletionMap, new Date(System.currentTimeMillis()));
    }

    @NonNull
    private IdentifierMap getSyncIdentifierMap(@NonNull File driveFile) {
        return new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, newSyncIdentifier(driveFile)));
    }

    @NonNull
    private Identifier newSyncIdentifier(@NonNull File driveFile) {
        return new Identifier(driveFile.getId());
    }

    @NonNull
    private SyncStatusMap newDriveSyncedStatusMap() {
        return new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true));
    }

}
