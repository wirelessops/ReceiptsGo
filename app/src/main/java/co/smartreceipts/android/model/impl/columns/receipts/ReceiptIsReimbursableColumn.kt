package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context

import co.smartreceipts.android.R
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptIsReimbursableColumn(
    id: Int, syncState: SyncState,
    private val localizedContext: Context, customOrderId: Long, uuid: UUID
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.REIMBURSABLE,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(receipt: Receipt): String =
        if (receipt.isReimbursable) localizedContext.getString(R.string.yes) else localizedContext.getString(
            R.string.no
        )

}
