package com.wops.automatic_backups.drive.rx

import com.wops.core.sync.model.SyncState
import com.wops.core.sync.model.impl.*
import com.wops.core.sync.provider.SyncProvider
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