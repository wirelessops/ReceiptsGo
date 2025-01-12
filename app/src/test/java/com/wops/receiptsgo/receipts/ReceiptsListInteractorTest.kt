package com.wops.receiptsgo.receipts

import com.wops.receiptsgo.apis.ApiValidationException
import com.wops.receiptsgo.imports.AttachmentSendFileImporter
import com.wops.receiptsgo.imports.intents.IntentImportProcessor
import com.wops.receiptsgo.imports.intents.model.IntentImportResult
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory
import com.wops.receiptsgo.ocr.OcrManager
import com.wops.receiptsgo.ocr.apis.model.OcrResponse
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.tooltip.image.data.ImageCroppingPreferenceStorage
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ReceiptsListInteractorTest {

    // Class under test
    private lateinit var interactor: ReceiptsListInteractor

    private val intentImportProcessor = mock<IntentImportProcessor>()
    private val attachmentSendFileImporter = mock<AttachmentSendFileImporter>()
    private val receiptTableController = mock<ReceiptTableController>()
    private val preferenceManager = mock<UserPreferenceManager>()
    private val ocrManager = mock<OcrManager>()
    private val imageCroppingPreferenceStorage = mock<ImageCroppingPreferenceStorage>()

    private val trip = mock<Trip>()
    private val intentImportResult = mock<IntentImportResult>()
    private val file = mock<File>()

    private val receipt = ReceiptBuilderFactory().setTrip(trip).build()

    private val lastImportProcessorResult: Subject<Optional<IntentImportResult>> = BehaviorSubject.createDefault(Optional.absent())

    @Before
    fun setUp() {

        whenever(intentImportProcessor.lastResult).thenReturn(lastImportProcessorResult)

        whenever(attachmentSendFileImporter.importAttachment(trip, receipt, intentImportResult)).thenReturn(Single.just(file))

        interactor = ReceiptsListInteractor(intentImportProcessor, attachmentSendFileImporter, receiptTableController, preferenceManager,
            ocrManager, imageCroppingPreferenceStorage, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun attachImportIntentEmptyTest() {

        interactor.attachImportIntent(trip, receipt).test()
            .assertNoErrors()
            .assertNotComplete()
            .assertEmpty()
    }

    @Test
    fun attachImportIntentTest() {

        lastImportProcessorResult.onNext(Optional.of(intentImportResult))

        interactor.attachImportIntent(trip, receipt).test()
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(file)
    }

    @Test
    fun updateReceiptImageTest() {

        val updatedReceipt = ReceiptBuilderFactory(receipt).setFile(file).build()

        whenever(receiptTableController.update(receipt, updatedReceipt, DatabaseOperationMetadata()))
            .thenReturn(Observable.just(Optional.of(updatedReceipt)))

        interactor.updateReceiptFile(receipt, file).test()
            .assertComplete()
            .assertNoErrors()

        verify(receiptTableController).update(receipt, updatedReceipt, DatabaseOperationMetadata())
    }

    @Test
    fun updateReceiptImageErrorTest() {

        val updatedReceipt = ReceiptBuilderFactory(receipt).setFile(file).build()

        whenever(receiptTableController.update(receipt, updatedReceipt, DatabaseOperationMetadata()))
            .thenReturn(Observable.just(Optional.absent()))

        interactor.updateReceiptFile(receipt, file).test()
            .assertNotComplete()
            .assertError(Exception::class.java)

        verify(receiptTableController).update(receipt, updatedReceipt, DatabaseOperationMetadata())
    }

    @Test
    fun scanReceiptImageTest() {

        val ocrResponse = mock<OcrResponse>()

        whenever(ocrManager.scan(file)).thenReturn(Observable.just(ocrResponse))

        interactor.scanReceiptImage(file)
        interactor.lastOcrResponseStream.test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertValue(Pair(file, ocrResponse))

        verify(ocrManager).scan(file)

        interactor.markLastOcrResponseAsProcessed()
        interactor.lastOcrResponseStream.test()
            .assertNoErrors()
            .assertNoValues()
            .assertNotComplete()
    }

    @Test
    fun scanReceiptImageErrorTest() {
        whenever(ocrManager.scan(file)).thenReturn(Observable.error(ApiValidationException("error")))

        interactor.scanReceiptImage(file)
        interactor.lastOcrResponseStream.test()
            .assertNotComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(ocrManager).scan(file)
    }
}