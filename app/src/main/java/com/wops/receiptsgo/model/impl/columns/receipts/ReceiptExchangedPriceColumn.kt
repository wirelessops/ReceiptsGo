package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context
import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.Receipt
import com.wops.core.sync.model.SyncState
import java.util.*

/**
 * Converts the [com.wops.receiptsgo.model.Receipt.price] based on the current exchange rate
 */
class ReceiptExchangedPriceColumn(
    id: Int, syncState: SyncState,
    localizedContext: Context, customOrderId: Long, uuid: UUID
) : AbstractExchangedPriceColumn(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PRICE_EXCHANGED,
    syncState,
    localizedContext,
    customOrderId,
    uuid
) {

    override fun getPrice(receipt: Receipt): Price = receipt.price
}
