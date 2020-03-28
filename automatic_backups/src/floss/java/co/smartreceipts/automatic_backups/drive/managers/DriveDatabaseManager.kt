package co.smartreceipts.automatic_backups.drive.managers

import android.content.Context
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.automatic_backups.drive.device.GoogleDriveSyncMetadata
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamsManager

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