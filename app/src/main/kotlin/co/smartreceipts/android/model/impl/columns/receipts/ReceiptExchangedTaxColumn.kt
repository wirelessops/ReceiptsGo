package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.sync.model.SyncState

/**
 * Converts the [co.smartreceipts.android.model.Receipt.getTax] based on the current exchange rate
 */
class ReceiptExchangedTaxColumn(
    id: Int, syncState: SyncState,
    localizedContext: Context, customOrderId: Long
) : AbstractExchangedPriceColumn(
    id,
    ReceiptColumnDefinitions.ActualDefinition.TAX_EXCHANGED,
    syncState,
    localizedContext,
    customOrderId
) {

    override fun getPrice(receipt: Receipt): Price = receipt.tax
}
