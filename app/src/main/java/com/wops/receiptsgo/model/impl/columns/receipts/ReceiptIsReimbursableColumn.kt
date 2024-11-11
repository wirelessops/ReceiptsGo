package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context

import com.wops.receiptsgo.R
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.core.sync.model.SyncState
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

    override fun getValue(rowItem: Receipt): String =
        if (rowItem.isReimbursable) localizedContext.getString(R.string.yes) else localizedContext.getString(
            R.string.no
        )

}
