package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptCategoryNameColumn @JvmOverloads constructor(
    id: Int,
    syncState: SyncState,
    customOrderId: Long = 0
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME,
    syncState,
    customOrderId
) {

    override fun getValue(receipt: Receipt): String = receipt.category.name
}
