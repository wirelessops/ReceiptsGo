package co.smartreceipts.automatic_backups.drive.rx

import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.impl.*
import co.smartreceipts.core.sync.provider.SyncProvider
import com.google.api.services.drive.model.File
import java.sql.Date
import java.util.*

class DriveStreamMappings {

    fun postInsertSyncState(): SyncState {
        return DefaultSyncState(
            null, newDriveSyncedStatusMap(),
            MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), Date(System.currentTimeMillis())
        )
    }

    fun postInsertSyncState(driveFile: File? = null): SyncState {

        val syncIdentifierMap: IdentifierMap? =
            if (driveFile != null) {
                getSyncIdentifierMap(driveFile)
            } else {
                null
            }

        return DefaultSyncState(
            syncIdentifierMap, newDriveSyncedStatusMap(),
            MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), Date(System.currentTimeMillis())
        )
    }

    fun postUpdateSyncState(driveFile: File): SyncState {
        return DefaultSyncState(
            getSyncIdentifierMap(driveFile), newDriveSyncedStatusMap(),
            MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), Date(System.currentTimeMillis())
        )
    }

    fun postDeleteSyncState(oldSyncState: SyncState, isFullDelete: Boolean): SyncState {
        val markedForDeletionMap: MarkedForDeletionMap =
            if (isFullDelete) {
                MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true))
            } else {
                MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, oldSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive)))
            }
        return DefaultSyncState(
            IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, null) as Map<SyncProvider, Identifier>),
            newDriveSyncedStatusMap(), markedForDeletionMap, Date(System.currentTimeMillis())
        )
    }

    private fun getSyncIdentifierMap(driveFile: File): IdentifierMap =
        IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, newSyncIdentifier(driveFile)))


    private fun newSyncIdentifier(driveFile: File): Identifier = Identifier(driveFile.id)


    private fun newDriveSyncedStatusMap(): SyncStatusMap = SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true))

}