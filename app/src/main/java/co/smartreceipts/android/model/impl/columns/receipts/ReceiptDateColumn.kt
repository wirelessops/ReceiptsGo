package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptDateColumn(
        id: Int,
        syncState: SyncState,
        private val dateFormatter: DateFormatter,
        customOrderId: Long,
        uuid: UUID
) : AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.DATE,
        syncState,
        customOrderId,
        uuid
) {

    override fun getValue(rowItem: Receipt): String = dateFormatter.getFormattedDate(rowItem.displayableDate)
}
