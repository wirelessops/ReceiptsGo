package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReportNameColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.REPORT_NAME,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(receipt: Receipt): String = receipt.trip.name

    override fun getFooter(rows: List<Receipt>): String =
        if (!rows.isEmpty()) getValue(rows[0]) else ""
}
