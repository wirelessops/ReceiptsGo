package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptIdColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.ID,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String = Integer.toString(rowItem.id)
}
