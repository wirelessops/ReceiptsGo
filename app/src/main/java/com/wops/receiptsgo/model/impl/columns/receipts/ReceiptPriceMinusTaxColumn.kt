package com.wops.receiptsgo.model.impl.columns.receipts

import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptPriceMinusTaxColumn(
    id: Int, syncState: SyncState,
    private val userPreferenceManager: UserPreferenceManager,
    customOrderId: Long, uuid: UUID
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PRICE_MINUS_TAX,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(rowItem: Receipt): String = getPrice(rowItem).decimalFormattedPrice

    override fun getFooter(rows: List<Receipt>): String {
        return if (!rows.isEmpty()) {
            val tripCurrency = rows[0].trip.tripCurrency
            val prices = ArrayList<Price>()
            for (receipt in rows) {
                prices.add(getPrice(receipt))
            }

            val total = PriceBuilderFactory().setPrices(prices, tripCurrency).build()

            if (total.isSingleCurrency) total.decimalFormattedPrice else total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }

    private fun getPrice(receipt: Receipt): Price {
        return if (userPreferenceManager[UserPreference.Receipts.UsePreTaxPrice]) {
            receipt.price
        } else {
            PriceBuilderFactory(receipt.price)
                .setPrice(receipt.price.price.subtract(receipt.tax.price).subtract(receipt.tax2.price))
                .build()
        }
    }
}
