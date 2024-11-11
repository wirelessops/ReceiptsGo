package com.wops.receiptsgo.receipts

import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.ocr.apis.model.OcrResponse
import com.wops.receiptsgo.persistence.database.controllers.impl.StubTableEventsListener
import com.wops.receiptsgo.widget.model.UiIndicator
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