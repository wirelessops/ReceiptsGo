package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the file path for a particular receipt
 */
class ReceiptFilePathColumn(id: Int, syncState: SyncState, customOrderId: Long) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.IMAGE_PATH,
        syncState,
        customOrderId
    ) {

    override fun getValue(receipt: Receipt): String? = receipt.filePath
}
