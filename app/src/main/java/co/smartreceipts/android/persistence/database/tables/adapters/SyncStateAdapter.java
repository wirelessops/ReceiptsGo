package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import java.sql.Date;
import java.util.Collections;

import javax.inject.Inject;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.android.sync.model.impl.SyncStatusMap;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class SyncStateAdapter {

    @Inject
    public SyncStateAdapter() {
    }


    @NonNull
    public SyncState read(@NonNull Cursor cursor) {
        final int driveIdentifierIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
        final int driveIsSyncedIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED);
        final int driveMarkedForDeletionIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION);
        final int lastLocalModificationTimeIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME);

        final String driveIdentifierString = cursor.getString(driveIdentifierIndex);
        final boolean driveIsSynced = cursor.getInt(driveIsSyncedIndex) > 0;
        final boolean driveMarkedForDeletion = cursor.getInt(driveMarkedForDeletionIndex) > 0;
        final long lastLocalModificationTimeLong = cursor.getLong(lastLocalModificationTimeIndex);

        final Date lastLocalModificationTime = new Date(lastLocalModificationTimeLong);

        final IdentifierMap identifierMap;
        if (driveIdentifierString != null) {
            final Identifier driveIdentifier = new Identifier(driveIdentifierString);
            identifierMap = new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, driveIdentifier));
        } else {
            identifierMap = null;
        }
        final SyncStatusMap syncStatusMap = new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, driveIsSynced));
        final MarkedForDeletionMap markedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, driveMarkedForDeletion));
        return new DefaultSyncState(identifierMap, syncStatusMap, markedForDeletionMap, lastLocalModificationTime);
    }

    @NonNull
    public ContentValues writeUnsynced(@NonNull SyncState syncState) {
        return write(syncState, true);
    }

    @NonNull
    public ContentValues write(@NonNull SyncState syncState) {
        return write(syncState, false);
    }

    @NonNull
    public ContentValues write(@NonNull SyncState syncState, boolean isUnsynced) {
        final ContentValues values = new ContentValues();
        final Identifier driveIdentifier = syncState.getSyncId(SyncProvider.GoogleDrive);
        if (driveIdentifier != null) {
            values.put(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, driveIdentifier.getId());
        } else {
            values.put(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, (String) null);
        }
        if (isUnsynced) {
            values.put(AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, false);
        } else {
            values.put(AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, syncState.isSynced(SyncProvider.GoogleDrive));
        }
        values.put(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION, syncState.isMarkedForDeletion(SyncProvider.GoogleDrive));
        values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, syncState.getLastLocalModificationTime().getTime());
        return values;
    }

    @NonNull
    public ContentValues deleteSyncData(@NonNull SyncProvider syncProvider) {
        final ContentValues values = new ContentValues();
        values.put(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, (String) null);
        values.put(AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, false);
        values.put(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION, false);
        values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, System.currentTimeMillis());
        return values;
    }

    @NonNull
    public SyncState get(@NonNull SyncState syncState, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            return syncState;
        } else {
            final Identifier driveIdentifier = syncState.getSyncId(SyncProvider.GoogleDrive);
            final IdentifierMap identifierMap;
            if (driveIdentifier != null) {
                identifierMap = new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, driveIdentifier));
            } else {
                identifierMap = null;
            }
            final SyncStatusMap syncStatusMap = new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, false));
            final MarkedForDeletionMap markedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, syncState.isMarkedForDeletion(SyncProvider.GoogleDrive)));
            return new DefaultSyncState(identifierMap, syncStatusMap, markedForDeletionMap, syncState.getLastLocalModificationTime());
        }
    }

}
