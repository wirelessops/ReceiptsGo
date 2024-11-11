package com.wops.automatic_backups.drive.rx

import android.content.Context
import android.os.Bundle
import com.wops.automatic_backups.drive.DriveServiceHelper
import com.wops.automatic_backups.drive.device.GoogleDriveSyncMetadata
import com.wops.core.sync.model.RemoteBackupMetadata
import com.wops.core.sync.model.SyncState
import com.wops.core.sync.model.impl.Identifier
import com.hadisatrio.optional.Optional
import com.google.api.services.drive.model.File
import io.reactivex.Single
import io.reactivex.subjects.Subject

/**
 * no-op
 */
class DriveStreamsManager(context: Context, driveServiceHelper: DriveServiceHelper, googleDriveSyncMetadata: GoogleDriveSyncMetadata,
                          driveErrorStream: Subject<Optional<Throwable>> ) {

    fun onConnected(bundle: Bundle?) {}

    fun onConnectionSuspended(cause: Int) {}

    fun getRemoteBackups(): Single<List<RemoteBackupMetadata>> = Single.never()

    fun uploadFileToDrive(currentSyncState: SyncState, file: java.io.File): Single<SyncState?> = Single.never()

    fun updateDriveFile(currentSyncState: SyncState, file: java.io.File): Single<SyncState?> = Single.never()

    fun renameBackup(identifier: Identifier, newFileName: String): Single<File> = Single.never()

    fun deleteDriveFile(currentSyncState: SyncState, isFullDelete: Boolean): Single<SyncState?> = Single.never()

    fun delete(identifier: Identifier): Single<Boolean?> = Single.never()

    fun clearCachedData() {}
}