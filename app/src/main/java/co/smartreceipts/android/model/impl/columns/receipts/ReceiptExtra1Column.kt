package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptExtra1Column(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.EXTRA_EDITTEXT_1,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String = rowItem.extraEditText1.orEmpty()
}
