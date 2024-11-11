package com.wops.receiptsgo.sync.drive.managers

import com.wops.automatic_backups.drive.managers.DriveDatabaseManager
import com.wops.core.di.scopes.ApplicationScope

@ApplicationScope
class NoOpGoogleDriveTableManager: GoogleDriveTableManager {
    override fun initializeListeners(driveDatabaseManager: DriveDatabaseManager, driveReceiptsManager: DriveReceiptsManager) { }

    override fun deinitializeListeners() {}
}