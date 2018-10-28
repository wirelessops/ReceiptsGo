package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptPriceColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.PRICE,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String = rowItem.price.decimalFormattedPrice

    override fun getFooter(rows: List<Receipt>): String {
        return if (!rows.isEmpty()) {
            val tripCurrency = rows[0].trip.tripCurrency
            val prices = ArrayList<Price>()
            for (receipt in rows) {
                prices.add(receipt.price)
            }

            val total = PriceBuilderFactory().setPrices(prices, tripCurrency).build()

            if (total.currencyCodeCount == 1) total.decimalFormattedPrice else total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }
}
