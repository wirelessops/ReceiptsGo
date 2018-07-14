package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context

import co.smartreceipts.android.R
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptIsPicturedColumn(
    id: Int, syncState: SyncState,
    private val localizedContext: Context, customOrderId: Long
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PICTURED,
    syncState,
    customOrderId
) {

    override fun getValue(receipt: Receipt): String {
        return when {
            receipt.hasImage() -> localizedContext.getString(R.string.yes)
            receipt.hasPDF() -> localizedContext.getString(R.string.yes_as_pdf)
            else -> localizedContext.getString(R.string.no)
        }
    }
}
