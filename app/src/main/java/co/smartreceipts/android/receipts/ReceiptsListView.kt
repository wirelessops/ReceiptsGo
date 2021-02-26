package co.smartreceipts.android.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.ocr.apis.model.OcrResponse
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable
import java.io.File

interface ReceiptsListView {

    val trip: Trip

    val itemClicks: Observable<Receipt>

    val itemImageClicks: Observable<Receipt>

    val actionBarUpdatesListener: StubTableEventsListener<Trip>


    fun getHighlightedReceipt(): Receipt?

    fun resetHighlightedReceipt()


    fun navigateToCreateReceipt(file: File?, ocrResponse: OcrResponse?)

    fun navigateToEditReceipt(receipt: Receipt)

    fun navigateToReceiptImage(receipt: Receipt)

    fun navigateToReceiptPdf(receipt: Receipt)

    fun navigateToCropActivity(file: File, requestCode: Int)


    fun showReceiptEditOptionsDialog(receipt: Receipt)

    fun showAttachmentDialog(receipt: Receipt)


    fun present(indicator: UiIndicator<Int>)
}