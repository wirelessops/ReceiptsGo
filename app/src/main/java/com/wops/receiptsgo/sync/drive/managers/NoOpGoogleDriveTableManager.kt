package com.wops.receiptsgo.sync.drive.managers

import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager
import co.smartreceipts.core.di.scopes.ApplicationScope

@ApplicationScope
class NoOpGoogleDriveTableManager: GoogleDriveTableManager {
    override fun initializeListeners(driveDatabaseManager: DriveDatabaseManager, driveReceiptsManager: DriveReceiptsManager) { }

    override fun deinitializeListeners() {}
}