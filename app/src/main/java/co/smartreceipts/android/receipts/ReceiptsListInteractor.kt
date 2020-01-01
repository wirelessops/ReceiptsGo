package co.smartreceipts.android.receipts

import android.content.Intent
import co.smartreceipts.android.imports.AttachmentSendFileImporter
import co.smartreceipts.android.imports.intents.IntentImportProcessor
import co.smartreceipts.android.imports.intents.model.IntentImportResult
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.ocr.OcrManager
import co.smartreceipts.android.ocr.apis.model.OcrResponse
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.image.data.ImageCroppingPreferenceStorage
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import javax.inject.Inject

@ApplicationScope
class ReceiptsListInteractor constructor(
    private val intentImportProcessor: IntentImportProcessor,
    private val attachmentSendFileImporter: AttachmentSendFileImporter,
    private val receiptTableController: ReceiptTableController,
    private val preferenceManager: UserPreferenceManager,
    private val ocrManager: OcrManager,
    private val imageCroppingPreferenceStorage: ImageCroppingPreferenceStorage,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    val lastOcrResponseStream: Observable<Pair<File, OcrResponse>>
        get() = lastOcrResponseSubject
            .filter { it.isPresent }
            .map { it.get() }


    private val lastOcrResponseSubject = BehaviorSubject.createDefault<Optional<Pair<File, OcrResponse>>>(Optional.absent())

    @Inject
    constructor(
        intentImportProcessor: IntentImportProcessor, attachmentSendFileImporter: AttachmentSendFileImporter,
        receiptTableController: ReceiptTableController, preferenceManager: UserPreferenceManager, ocrManager: OcrManager,
        imageCroppingPreferenceStorage: ImageCroppingPreferenceStorage
    )
            : this(
        intentImportProcessor, attachmentSendFileImporter, receiptTableController, preferenceManager,
        ocrManager, imageCroppingPreferenceStorage, Schedulers.io(), AndroidSchedulers.mainThread()
    )

    fun getLastImportIntentResult(): Observable<Optional<IntentImportResult>> {
        return intentImportProcessor.lastResult
    }

    fun attachImportIntent(trip: Trip, receipt: Receipt): Observable<File> {
        return getLastImportIntentResult()
            .filter { it.isPresent }
            .map { it.get() }
            .flatMapSingle { intentImportResult ->
                attachmentSendFileImporter.importAttachment(trip, receipt, intentImportResult)
            }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun updateReceiptFile(receipt: Receipt, file: File): Completable {
        val updatedReceipt = ReceiptBuilderFactory(receipt)
            .setFile(file)
            .build()

        return receiptTableController.update(receipt, updatedReceipt, DatabaseOperationMetadata())
            .flatMapCompletable { optional ->
                if (optional.isPresent) {
                    Completable.complete()
                } else {
                    Completable.error(Exception("Failed to update receipt file"))
                }
            }
    }

    fun scanReceiptImage(file: File) {
        ocrManager.scan(file)
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
            .subscribe({ lastOcrResponseSubject.onNext(Optional.of(Pair(file, it))) }, { lastOcrResponseSubject.onNext(Optional.absent()) })
    }

    fun isCropScreenEnabled(): Boolean = preferenceManager.get(UserPreference.General.EnableCrop)

    fun markIntentAsSuccessfullyProcessed(intent: Intent) = intentImportProcessor.markIntentAsSuccessfullyProcessed(intent)

    fun markLastOcrResponseAsProcessed() = lastOcrResponseSubject.onNext(Optional.absent())

    fun setCroppingScreenWasShown() = imageCroppingPreferenceStorage.setCroppingScreenWasShown(true)
}