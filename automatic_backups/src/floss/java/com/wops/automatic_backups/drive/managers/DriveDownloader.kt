package com.wops.automatic_backups.drive.managers

import com.wops.automatic_backups.drive.rx.DriveStreamsManager
import com.wops.core.sync.model.RemoteBackupMetadata
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import java.io.File

/**
 * no-op
 */
class DriveDownloader(private val driveStreamsManager: DriveStreamsManager) {

    fun downloadAllFilesInDriveFolder(remoteBackupMetadata: RemoteBackupMetadata, downloadLocation: File): Single<List<File?>?> = Single.never()

    fun downloadTmpDatabaseFile(remoteBackupMetadata: RemoteBackupMetadata, downloadLocation: File): Single<Optional<File>> = Single.never()

    fun downloadFile(fileId: String, downloadLocationFile: File): Single<Optional<File>> = Single.never()
}