package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptNameColumn(id: Int, syncState: SyncState, customOrderId: Long) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.NAME,
        syncState,
        customOrderId
    ) {

    override fun getValue(receipt: Receipt): String = receipt.name
}