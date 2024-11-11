package com.wops.receiptsgo.imports

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.wops.receiptsgo.imports.exceptions.InvalidPdfException
import com.wops.receiptsgo.imports.utils.PdfValidator
import com.wops.receiptsgo.model.Trip
import com.wops.core.utils.UriUtils
import com.wops.analytics.log.Logger
import io.reactivex.Single
import wb.android.storage.StorageManager
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class GenericFileImportProcessor @VisibleForTesting
internal constructor(
    private val trip: Trip,
    private val storageManager: StorageManager,
    private val contentResolver: ContentResolver,
    private val pdfValidator: PdfValidator
) : FileImportProcessor {

    constructor(trip: Trip, storageManager: StorageManager, context: Context)
            : this(trip, storageManager, context.contentResolver, PdfValidator(context))


    override fun process(uri: Uri): Single<File> {
        Logger.info(this@GenericFileImportProcessor, "Attempting to import: {}", uri)
        return Single.create { emitter ->
            var inputStream: InputStream? = null
            try {
                inputStream = contentResolver.openInputStream(uri)
                val destination =
                    storageManager.getFile(
                        trip.directory,
                        System.currentTimeMillis().toString() + "." + UriUtils.getExtension(uri, contentResolver)
                    )

                if (storageManager.copy(inputStream, destination, true)) {
                    if (!pdfValidator.isPdfValid(destination)) {
                        emitter.onError(InvalidPdfException("Selected PDF looks like non valid"))
                    } else {
                        emitter.onSuccess(destination)
                        Logger.info(this@GenericFileImportProcessor, "Successfully copied Uri to the Smart Receipts directory")
                    }
                } else {
                    emitter.onError(FileNotFoundException())
                }
            } catch (e: IOException) {
                Logger.error(this@GenericFileImportProcessor, "Failed to import uri", e)
                emitter.onError(e)
            } finally {
                StorageManager.closeQuietly(inputStream)
            }
        }
    }

}
