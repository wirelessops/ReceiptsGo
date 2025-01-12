package com.wops.automatic_backups.drive.managers

import android.content.Context
import com.wops.analytics.Analytics
import com.wops.automatic_backups.drive.device.GoogleDriveSyncMetadata
import com.wops.automatic_backups.drive.rx.DriveStreamsManager

/**
 * no-op
 */
class DriveDatabaseManager(
    context: Context, driveTaskManager: DriveStreamsManager,
    googleDriveSyncMetadata: GoogleDriveSyncMetadata,
    analytics: Analytics
) {
    fun syncDatabase() {}
}