package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context

import com.wops.receiptsgo.R
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptIsPicturedColumn(
    id: Int, syncState: SyncState,
    private val localizedContext: Context, customOrderId: Long, uuid: UUID
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PICTURED,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(rowItem: Receipt): String {
        return when {
            rowItem.hasImage() -> localizedContext.getString(R.string.yes)
            rowItem.hasPDF() -> localizedContext.getString(R.string.yes_as_pdf)
            else -> localizedContext.getString(R.string.no)
        }
    }
}
