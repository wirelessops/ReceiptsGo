package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptCurrencyCodeColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.CURRENCY,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String = rowItem.price.currencyCode

    override fun getFooter(rows: List<Receipt>): String {
        return if (rows.isNotEmpty()) {
            val tripCurrency = rows[0].trip.tripCurrency
            PriceBuilderFactory().setPriceables(rows, tripCurrency).build().currencyCode
        } else {
            ""
        }
    }
}
