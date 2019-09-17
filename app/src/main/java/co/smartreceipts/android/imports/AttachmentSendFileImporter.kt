package co.smartreceipts.android.imports

import android.content.Context
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.ErrorEvent
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.imports.intents.model.FileType
import co.smartreceipts.android.imports.intents.model.IntentImportResult
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.settings.UserPreferenceManager
import io.reactivex.Single
import wb.android.storage.StorageManager
import java.io.File
import javax.inject.Inject

@ApplicationScope
class AttachmentSendFileImporter @Inject constructor(
    private val context: Context,
    private val storageManager: StorageManager,
    private val preferences: UserPreferenceManager,
    private val receiptTableController: ReceiptTableController,
    private val analytics: Analytics
) {


    fun importAttachment(trip: Trip, receipt: Receipt, intentImportResult: IntentImportResult): Single<File> {

        val importProcessor: FileImportProcessor = when {
            intentImportResult.fileType == FileType.Image -> ImageImportProcessor(trip, storageManager, preferences, context)
            intentImportResult.fileType == FileType.Pdf -> GenericFileImportProcessor(trip, storageManager, context)
            else -> AutoFailImportProcessor()
        }

        return importProcessor.process(intentImportResult.uri)
            .doOnSuccess { file ->
                receiptTableController.update(
                    receipt,
                    ReceiptBuilderFactory(receipt).setFile(file).build(), DatabaseOperationMetadata()
                )
            }
            .doOnError { throwable -> analytics.record(ErrorEvent(this@AttachmentSendFileImporter, throwable)) }
    }

}
