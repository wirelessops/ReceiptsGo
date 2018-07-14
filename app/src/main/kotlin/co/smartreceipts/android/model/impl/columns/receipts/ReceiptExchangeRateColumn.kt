package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptExchangeRateColumn(id: Int, syncState: SyncState, customOrderId: Long) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.EXCHANGE_RATE,
        syncState,
        customOrderId
    ) {

    override fun getValue(receipt: Receipt): String =
        receipt.price.exchangeRate.getDecimalFormattedExchangeRate(receipt.trip.tripCurrency)
}
