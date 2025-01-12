package com.wops.receiptsgo.imports

import android.content.Context
import com.wops.analytics.Analytics
import com.wops.analytics.events.ErrorEvent
import com.wops.receiptsgo.imports.intents.model.FileType
import com.wops.receiptsgo.imports.intents.model.IntentImportResult
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.core.di.scopes.ApplicationScope
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

        val importProcessor: FileImportProcessor = when (intentImportResult.fileType) {
            FileType.Image -> ImageImportProcessor(trip, storageManager, preferences, context)
            FileType.Pdf -> GenericFileImportProcessor(trip, storageManager, context)
            else -> AutoFailImportProcessor()
        }

        return importProcessor.process(intentImportResult.uri)
            .doOnSuccess { file ->
                receiptTableController.update(
                    receipt,
                    ReceiptBuilderFactory(receipt).setFile(file).build(), DatabaseOperationMetadata()
                )
            }
            .doOnError { throwable -> analytics.record(
                ErrorEvent(
                    this@AttachmentSendFileImporter,
                    throwable
                )
            ) }
    }

}
