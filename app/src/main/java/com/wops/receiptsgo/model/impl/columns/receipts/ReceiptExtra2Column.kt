package com.wops.receiptsgo.model.impl.columns.receipts

import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptExtra2Column(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.EXTRA_EDITTEXT_2,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String = rowItem.extraEditText2.orEmpty()
}
