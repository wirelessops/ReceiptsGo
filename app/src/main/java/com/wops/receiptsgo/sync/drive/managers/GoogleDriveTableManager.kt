package com.wops.receiptsgo.sync.drive.managers

import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager

interface GoogleDriveTableManager {

    fun initializeListeners(
        driveDatabaseManager: DriveDatabaseManager,
        driveReceiptsManager: DriveReceiptsManager
    )

    fun deinitializeListeners()
}