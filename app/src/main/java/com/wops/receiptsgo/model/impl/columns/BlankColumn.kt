package com.wops.receiptsgo.model.impl.columns

import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns blank values for everything but the header
 */
class BlankColumn<T> constructor(
    id: Int,
    syncState: SyncState,
    customOrderId: Long,
    uuid: UUID
) : AbstractColumnImpl<T>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.BLANK,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(rowItem: T) = ""

    override fun getFooter(rows: List<T>) = ""
}
