package co.smartreceipts.automatic_backups.drive.rx

import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.impl.*
import co.smartreceipts.core.sync.provider.SyncProvider
import java.sql.Date
import java.util.*

class DriveStreamMappings {

    fun postInsertSyncState(): SyncState {
        return DefaultSyncState(
            null, newDriveSyncedStatusMap(),
            MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), Date(System.currentTimeMillis())
        )
    }

    private fun newDriveSyncedStatusMap(): SyncStatusMap = SyncStatusMap(Collections.singletonMap(SyncProvider.None, true))


}