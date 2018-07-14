package co.smartreceipts.android.model.impl.columns

import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns blank values for everything but the header
 */
class BlankColumn<T> @JvmOverloads constructor(
    id: Int,
    syncState: SyncState,
    customOrderId: Long = 0
) : AbstractColumnImpl<T>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.BLANK,
    syncState,
    customOrderId
) {

    override fun getValue(rowItem: T) = ""

    override fun getFooter(rows: List<T>) = ""
}
