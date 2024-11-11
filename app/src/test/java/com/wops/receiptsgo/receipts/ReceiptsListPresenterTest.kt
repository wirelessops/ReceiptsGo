package com.wops.receiptsgo.receipts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import co.smartreceipts.analytics.Analytics
import com.wops.receiptsgo.images.CropImageActivity
import com.wops.receiptsgo.imports.RequestCodes
import com.wops.receiptsgo.imports.importer.ActivityFileResultImporter
import com.wops.receiptsgo.imports.importer.ActivityFileResultImporterResponse
import com.wops.receiptsgo.imports.locator.ActivityFileResultLocator
import com.wops.receiptsgo.imports.locator.ActivityFileResultLocatorResponse
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.ocr.apis.model.OcrResponse
import com.wops.receiptsgo.ocr.widget.alert.OcrStatusAlerterPresenter
import com.wops.receiptsgo.permissions.PermissionsDelegate
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import com.wops.receiptsgo.receipts.attacher.ReceiptAttachmentManager
import com.wops.receiptsgo.widget.model.UiIndicator
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ReceiptsListPresenterTest {

    // Class under test
    lateinit var presenter: ReceiptsListPresenter

    private val view = mock<ReceiptsListView>()
    private val interactor = mock<ReceiptsListInteractor>()

    private val ocrStatusAlerterPresenter = mock<OcrStatusAlerterPresenter>()
    private val locator = mock<ActivityFileResultLocator>()
    private val importer = mock<ActivityFileResultImporter>()
    private val permissionsDelegate = mock<PermissionsDelegate>()
    private val tripTableController = mock<TripTableController>()
    private val analytics = mock<Analytics>()
    private val receiptAttachmentManager = mock<ReceiptAttachmentManager>()

    private val locatorResultStream = PublishSubject.create<ActivityFileResultLocatorResponse>()
    private val importerResultStream = PublishSubject.create<ActivityFileResultImporterResponse>()
    private val ocrResultStream = PublishSubject.create<Pair<File, OcrResponse>>()

    private val uri = mock<Uri>()
    private val trip = mock<Trip>()
    private val file = mock<File>()
    private val ocrResponse = mock<OcrResponse>()
    private val intent = mock<Intent>()

    companion object {
        val requestCode = RequestCodes.NEW_RECEIPT_CAMERA_IMAGE
        val requestCodeCrop = RequestCodes.NEW_RECEIPT_CAMERA_IMAGE_CROP
        val resultCode = Activity.RESULT_OK
        const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val locatorResponse: ActivityFileResultLocatorResponse =
        ActivityFileResultLocatorResponse.locatorResponse(uri, requestCode, resultCode)
    private val importerResponse: ActivityFileResultImporterResponse =
        ActivityFileResultImporterResponse.importerResponse(file, requestCode, resultCode)

    @Before
    fun setUp() {

        whenever(view.trip).thenReturn(trip)
        whenever(view.itemClicks).thenReturn(Observable.never())
        whenever(view.itemImageClicks).thenReturn(Observable.never())
        whenever(view.actionBarUpdatesListener).thenReturn(mock())

        whenever(interactor.isCropScreenEnabled()).thenReturn(false)
        whenever(interactor.lastOcrResponseStream).thenReturn(ocrResultStream)
        whenever(interactor.scanReceiptImage(any())).then {
            ocrResultStream.onNext(
                Pair(
                    file,
                    ocrResponse
                )
            )
        }
        whenever(interactor.getLastImportIntentResult()).thenReturn(Observable.never())

        whenever(permissionsDelegate.checkPermissionAndMaybeAsk(READ_PERMISSION)).thenReturn(
            Completable.complete()
        )

        whenever(locator.uriStream).thenReturn(locatorResultStream)
        whenever(
            locator.onActivityResult(
                any(),
                any(),
                any(),
                any()
            )
        ).doAnswer { locatorResultStream.onNext(locatorResponse) }

        whenever(importer.resultStream).thenReturn(importerResultStream)
        whenever(
            importer.importFile(
                any(),
                any(),
                any(),
                any()
            )
        ).doAnswer { importerResultStream.onNext(importerResponse) }


        presenter = ReceiptsListPresenter(
            view, interactor, ocrStatusAlerterPresenter, locator,
            importer, permissionsDelegate, tripTableController, receiptAttachmentManager, analytics,
            Schedulers.trampoline()
        )
    }

    @Test
    fun subscribeUnsubscribeTest() {
        presenter.subscribe()

        verify(ocrStatusAlerterPresenter).subscribe()
        verify(tripTableController).subscribe(any())

        presenter.unsubscribe()

        verify(ocrStatusAlerterPresenter).unsubscribe()
        verify(tripTableController).unsubscribe(any())
    }

    @Test
    fun handleActivityResultFromCameraWithoutCrop() {

        presenter.subscribe()
        presenter.handleActivityResult(requestCode, resultCode, intent, uri)

        val inOrder = inOrder(locator, permissionsDelegate, importer, interactor, view)

        inOrder.verify(locator).onActivityResult(requestCode, resultCode, intent, uri)
        inOrder.verify(permissionsDelegate)
            .checkPermissionAndMaybeAsk(Manifest.permission.READ_EXTERNAL_STORAGE)
        inOrder.verify(permissionsDelegate)
            .markRequestConsumed(Manifest.permission.READ_EXTERNAL_STORAGE)

        inOrder.verify(view).present(UiIndicator.loading())
        inOrder.verify(importer).importFile(requestCode, Activity.RESULT_OK, uri, trip)

        inOrder.verify(interactor).scanReceiptImage(file)

        inOrder.verify(view).navigateToCreateReceipt(file, ocrResponse)

        inOrder.verify(interactor).markLastOcrResponseAsProcessed()
        inOrder.verify(locator).markThatResultsWereConsumed()
        inOrder.verify(importer).markThatResultsWereConsumed()
    }

    @Test
    fun handleActivityResultFromCameraWithCrop() {

        whenever(interactor.isCropScreenEnabled()).thenReturn(true)

        presenter.subscribe()
        presenter.handleActivityResult(requestCode, resultCode, intent, uri)

        val inOrder = inOrder(locator, permissionsDelegate, importer, interactor, view)

        inOrder.verify(locator).onActivityResult(requestCode, resultCode, intent, uri)
        inOrder.verify(permissionsDelegate)
            .checkPermissionAndMaybeAsk(Manifest.permission.READ_EXTERNAL_STORAGE)
        inOrder.verify(permissionsDelegate)
            .markRequestConsumed(Manifest.permission.READ_EXTERNAL_STORAGE)

        inOrder.verify(view).present(UiIndicator.loading())
        inOrder.verify(importer).importFile(requestCode, Activity.RESULT_OK, uri, trip)
        inOrder.verify(view).navigateToCropActivity(file, requestCodeCrop)
    }

    @Test
    fun handleActivityResultFromCrop() {
        whenever(interactor.isCropScreenEnabled()).thenReturn(true)

        whenever(intent.getStringExtra(CropImageActivity.EXTRA_IMAGE_PATH)).thenReturn("path")

        presenter.subscribe()
        presenter.handleActivityResult(requestCodeCrop, resultCode, intent, uri)

        val inOrder = inOrder(interactor, view, locator, importer)

        inOrder.verify(interactor).setCroppingScreenWasShown()

        inOrder.verify(interactor).scanReceiptImage(any())
        inOrder.verify(view).navigateToCreateReceipt(any(), eq(ocrResponse))

        inOrder.verify(interactor).markLastOcrResponseAsProcessed()
        inOrder.verify(locator).markThatResultsWereConsumed()
        inOrder.verify(importer).markThatResultsWereConsumed()
    }

    @Test
    fun handleActivityResultFromCropError() {
        whenever(interactor.isCropScreenEnabled()).thenReturn(true)
        whenever(intent.getStringExtra(CropImageActivity.EXTRA_IMAGE_PATH)).thenReturn("path")

        presenter.subscribe()
        importerResultStream.onNext(importerResponse)
        presenter.handleActivityResult(
            requestCodeCrop,
            CropImageActivity.RESULT_CROP_ERROR,
            intent,
            uri
        )

        verify(interactor, never()).scanReceiptImage(any())
        verify(locator, atLeastOnce()).markThatResultsWereConsumed()
        verify(importer, atLeastOnce()).markThatResultsWereConsumed()
    }
}