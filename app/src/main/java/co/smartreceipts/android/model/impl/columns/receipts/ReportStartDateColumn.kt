package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReportStartDateColumn(
        id: Int,
        syncState: SyncState,
        private val dateFormatter: DateFormatter,
        customOrderId: Long,
        uuid: UUID
) : AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.REPORT_START_DATE,
        syncState,
        customOrderId,
        uuid
) {

    override fun getValue(rowItem: Receipt): String {
        val trip = rowItem.trip
        return dateFormatter.getFormattedDate(trip.startDate, trip.startTimeZone)
    }


    override fun getFooter(rows: List<Receipt>): String =
        if (!rows.isEmpty()) getValue(rows[0]) else ""

}
