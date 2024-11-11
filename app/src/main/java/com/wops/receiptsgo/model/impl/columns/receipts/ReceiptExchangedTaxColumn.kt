package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context

import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Converts the [com.wops.receiptsgo.model.Receipt.getTax] plus [com.wops.receiptsgo.model.Receipt.getTax2] based on the current exchange rate
 */
class ReceiptExchangedTaxColumn(
    id: Int, syncState: SyncState,
    localizedContext: Context, customOrderId: Long, uuid: UUID
) : AbstractExchangedPriceColumn(
    id,
    ReceiptColumnDefinitions.ActualDefinition.TAX_EXCHANGED,
    syncState,
    localizedContext,
    customOrderId,
    uuid
) {

    override fun getPrice(receipt: Receipt): Price =
        PriceBuilderFactory(receipt.tax).setPrice(receipt.tax.price.add(receipt.tax2.price)).setCurrency(receipt.trip.tripCurrency).build()
}
