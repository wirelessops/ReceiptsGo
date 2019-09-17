package co.smartreceipts.android.receipts

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import co.smartreceipts.android.R
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.images.CropImageActivity
import co.smartreceipts.android.imports.RequestCodes
import co.smartreceipts.android.imports.importer.ActivityFileResultImporter
import co.smartreceipts.android.imports.intents.model.FileType
import co.smartreceipts.android.imports.locator.ActivityFileResultLocator
import co.smartreceipts.android.imports.locator.ActivityFileResultLocatorResponse
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.ocr.widget.alert.OcrStatusAlerterPresenter
import co.smartreceipts.android.permissions.PermissionsDelegate
import co.smartreceipts.android.permissions.exceptions.PermissionsNotGrantedException
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.receipts.creator.ReceiptCreateActionPresenter
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.widget.model.UiIndicator
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.File
import javax.inject.Inject

@FragmentScope
class ReceiptsListPresenter @Inject constructor(
    view: ReceiptsListView, interactor: ReceiptsListInteracror,
    private val ocrStatusAlerterPresenter: OcrStatusAlerterPresenter,
    private val receiptCreateActionPresenter: ReceiptCreateActionPresenter,
    private val activityFileResultLocator: ActivityFileResultLocator,
    private val activityFileResultImporter: ActivityFileResultImporter,
    private val permissionsDelegate: PermissionsDelegate,
    private val tripTableController: TripTableController,
    private val analytics: Analytics
) :
    BaseViperPresenter<ReceiptsListView, ReceiptsListInteracror>(view, interactor) {

    companion object {
        const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private var importIntentMode: Boolean = false


    override fun subscribe() {

        ocrStatusAlerterPresenter.subscribe()
        receiptCreateActionPresenter.subscribe()

        tripTableController.subscribe(view.actionBarUpdatesListener)

        compositeDisposable.add(view.itemClicks
            .subscribe { receipt ->
                when {
                    !importIntentMode -> {
                        analytics.record(Events.Receipts.ReceiptMenuEdit)
                        view.navigateToEditReceipt(receipt)
                    }
                    else -> attachImportIntent(receipt)
                }
            }
        )

        compositeDisposable.add(view.itemMenuClicks
            .subscribe { receipt ->
                if (!importIntentMode) {
                    view.showReceiptMenu(receipt)
                }
            }
        )

        compositeDisposable.add(view.itemImageClicks
            .subscribe { receipt ->
                if (!importIntentMode) {
                    when {
                        receipt.hasImage() -> {
                            analytics.record(Events.Receipts.ReceiptMenuViewImage)
                            view.navigateToReceiptImage(receipt)
                        }
                        receipt.hasPDF() -> {
                            analytics.record(Events.Receipts.ReceiptMenuViewPdf)
                            view.navigateToReceiptPdf(receipt)
                        }
                        else -> {
                            view.showAttachmentDialog(receipt)
                        }
                    }
                } else {
                    attachImportIntent(receipt)
                }
            }
        )

        compositeDisposable.add(interactor.getLastImportIntentResult()
            .map { intentImportResultOptional ->
                intentImportResultOptional.isPresent && (intentImportResultOptional.get().fileType == FileType.Image
                        || intentImportResultOptional.get().fileType == FileType.Pdf)
            }
            .subscribe { importIntentMode = it }
        )


        compositeDisposable.add(activityFileResultLocator.uriStream
            .flatMapSingle { response ->
                Logger.debug(this, "getting response from activityFileResultLocator.getUriStream() uri {}", response.uri.toString())

                val scheme = response.uri?.scheme
                if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
                    return@flatMapSingle Single.just(response)
                } else { // we need to check read external storage permission
                    Logger.debug(this, "need to check permission")
                    return@flatMapSingle permissionsDelegate.checkPermissionAndMaybeAsk(READ_PERMISSION)
                        .toSingleDefault(response)
                        .onErrorReturn { ActivityFileResultLocatorResponse.locatorError(it) }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { locatorResponse ->
                permissionsDelegate.markRequestConsumed(READ_PERMISSION)
                if (!locatorResponse.throwable.isPresent) {
                    view.present(UiIndicator.loading())
                    activityFileResultImporter.importFile(
                        locatorResponse.requestCode,
                        locatorResponse.resultCode, locatorResponse.uri!!, view.trip
                    )
                } else {
                    Logger.debug(this, "Error with permissions")
                    if (locatorResponse.throwable.get() is PermissionsNotGrantedException) {
                        view.present(UiIndicator.error(R.string.toast_no_storage_permissions))
                    } else {
                        view.present(UiIndicator.error(R.string.FILE_SAVE_ERROR))
                    }

                    view.resetHighlightedReceipt()
                    view.present(UiIndicator.success())

                    Logger.debug(this, "marking that locator result were consumed")
                    activityFileResultLocator.markThatResultsWereConsumed()
                    Logger.debug(this, "marked that locator result were consumed")

                }

            }
        )

        compositeDisposable.add(activityFileResultImporter.resultStream
            .subscribe { response ->
                Logger.info(this, "Handled the import of {}", response)
                if (!response.throwable.isPresent) {
                    val file: File = response.file!!
                    when (response.requestCode) {
                        RequestCodes.NEW_RECEIPT_IMPORT_IMAGE,
                        RequestCodes.NEW_RECEIPT_CAMERA_IMAGE -> {
                            if (interactor.isCropScreenEnabled()) {
                                val requestCode = when {
                                    response.requestCode == RequestCodes.NEW_RECEIPT_IMPORT_IMAGE -> RequestCodes.NEW_RECEIPT_IMPORT_IMAGE_CROP
                                    else -> RequestCodes.NEW_RECEIPT_CAMERA_IMAGE_CROP
                                }
                                view.navigateToCropActivity(file, requestCode)
                            } else {
                                performOcrScan(file)
                            }
                        }

                        RequestCodes.NEW_RECEIPT_IMPORT_PDF -> {
                            performOcrScan(file)
                        }

                        RequestCodes.ATTACH_GALLERY_IMAGE,
                        RequestCodes.ATTACH_CAMERA_IMAGE -> {
                            if (interactor.isCropScreenEnabled()) {
                                val cropRequestCode = when {
                                    response.requestCode == RequestCodes.ATTACH_GALLERY_IMAGE -> RequestCodes.ATTACH_GALLERY_IMAGE_CROP
                                    else -> RequestCodes.ATTACH_CAMERA_IMAGE_CROP
                                }
                                view.navigateToCropActivity(file, cropRequestCode)
                            } else {
                                updateHighlightedReceiptFile(file)
                            }
                        }

                        RequestCodes.ATTACH_GALLERY_PDF -> updateHighlightedReceiptFile(file)
                    }
                } else {
                    view.present(UiIndicator.error(R.string.FILE_SAVE_ERROR))
                }

                view.present(UiIndicator.success())

                // Indicate that we consumed these results to avoid using this same stream on the next event
                activityFileResultLocator.markThatResultsWereConsumed()
                activityFileResultImporter.markThatResultsWereConsumed()
            }
        )

        compositeDisposable.add(
            interactor.lastOcrResponseStream
                .subscribe({
                    view.present(UiIndicator.idle())
                    view.navigateToCreateReceipt(it.first, it.second)

                    interactor.markLastOcrResponseAsProcessed()
                },
                    { view.present(UiIndicator.idle()) })
        )

    }

    override fun unsubscribe() {
        super.unsubscribe()

        ocrStatusAlerterPresenter.unsubscribe()
        receiptCreateActionPresenter.unsubscribe()

        tripTableController.unsubscribe(view.actionBarUpdatesListener)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, cachedImageSaveLocation: Uri?) {

        if (RequestCodes.CROP_REQUESTS.contains(requestCode).not()) { // result from Camera
            activityFileResultLocator.onActivityResult(requestCode, resultCode, data, cachedImageSaveLocation)
        } else { // result from Crop Activity

            if (resultCode != CropImageActivity.RESULT_CROP_ERROR) {

                interactor.setCroppingScreenWasShown()

                val imagePath = data?.getStringExtra(CropImageActivity.EXTRA_IMAGE_PATH)

                when (requestCode) {
                    RequestCodes.NEW_RECEIPT_CAMERA_IMAGE_CROP,
                    RequestCodes.NEW_RECEIPT_IMPORT_IMAGE_CROP -> {
                        imagePath?.let { performOcrScan(File(it)) }
                    }

                    RequestCodes.ATTACH_GALLERY_IMAGE_CROP,
                    RequestCodes.ATTACH_CAMERA_IMAGE_CROP -> {
                        imagePath?.let { updateHighlightedReceiptFile(File(it)) }
                    }

                }

            } else {
                Logger.error(this, "An error occurred while cropping the image")
            }

            activityFileResultLocator.markThatResultsWereConsumed()
            activityFileResultImporter.markThatResultsWereConsumed()
        }

        if (resultCode != RESULT_OK) {
            view.present(UiIndicator.idle())
        }
    }

    fun markIntentAsProcessed(intent: Intent) {
        compositeDisposable.add(interactor.getLastImportIntentResult()
            .filter { optionalResult -> optionalResult.isPresent }
            .map { it.get() }
            .filter { result -> result.fileType == FileType.Image || result.fileType == FileType.Pdf }
            .subscribe { interactor.markIntentAsSuccessfullyProcessed(intent) }
        )
    }

    private fun attachImportIntent(receipt: Receipt) {
        compositeDisposable.add(
            interactor.attachImportIntent(view.trip, receipt)
                .subscribe({}, { view.present(UiIndicator.error(R.string.database_error)) })
        )
    }

    private fun updateHighlightedReceiptFile(file: File) {
        view.getHighlightedReceipt()!!.let {
            compositeDisposable.add(
                interactor.updateReceiptFile(it, file)
                    .subscribe({}, { view.present(UiIndicator.error(R.string.FILE_SAVE_ERROR)) })
            )
        }

        view.resetHighlightedReceipt()
    }

    private fun performOcrScan(file: File) {
        view.present(UiIndicator.loading())

        interactor.scanReceiptImage(file)
    }
}