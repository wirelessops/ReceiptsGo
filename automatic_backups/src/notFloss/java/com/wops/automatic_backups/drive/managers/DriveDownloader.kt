package com.wops.automatic_backups.drive.managers

import com.wops.analytics.log.Logger
import com.wops.automatic_backups.drive.rx.DriveStreamsManager
import com.wops.core.persistence.DatabaseConstants
import com.wops.core.sync.model.RemoteBackupMetadata
import com.google.api.services.drive.model.FileList
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import java.io.File
import java.util.*
import com.google.api.services.drive.model.File as DriveFile

class DriveDownloader(private val driveStreamsManager: DriveStreamsManager) {

    fun downloadAllFilesInDriveFolder(remoteBackupMetadata: RemoteBackupMetadata, downloadLocation: File): Single<List<File?>?> {

        return driveStreamsManager.getFilesInFolder(remoteBackupMetadata.id.id)
            .flatMap { fileList: FileList ->
                val javaFileList: MutableList<File> = ArrayList()

                for (file: DriveFile in fileList.files) {
                    val filename = file.id + "__" + file.originalFilename
                    val optionalFile = driveStreamsManager.download(file.id, File(downloadLocation, filename)).blockingGet()
                    if (optionalFile.isPresent) {
                        javaFileList.add(optionalFile.get())
                    }
                }

                Single.just(javaFileList)
            }
    }

    fun downloadTmpDatabaseFile(remoteBackupMetadata: RemoteBackupMetadata, downloadLocation: File): Single<Optional<File>> {
        Logger.debug(this, "Fetching receipts database in drive for this folder")

        return driveStreamsManager.getFilesInFolder(remoteBackupMetadata.id.id, DatabaseConstants.DATABASE_NAME)
            .flatMap {fileList ->
                Logger.debug(this@DriveDownloader, "Downloading database file")
                val tempDbFile = File(downloadLocation, DatabaseConstants.DATABASE_EXPORT_NAME)
                driveStreamsManager.download(fileList.files[0].id, tempDbFile)
            }
    }

    fun downloadFile(fileId: String, downloadLocationFile: File): Single<Optional<File>> {
        return driveStreamsManager.download(fileId, downloadLocationFile)
    }
}