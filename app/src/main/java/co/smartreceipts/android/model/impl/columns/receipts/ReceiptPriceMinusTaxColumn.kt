package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.core.sync.model.SyncState
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
