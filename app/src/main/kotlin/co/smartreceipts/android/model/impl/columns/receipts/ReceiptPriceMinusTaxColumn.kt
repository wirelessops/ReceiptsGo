package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptPriceMinusTaxColumn(
    id: Int, syncState: SyncState,
    private val userPreferenceManager: UserPreferenceManager,
    customOrderId: Long
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PRICE_MINUS_TAX,
    syncState,
    customOrderId
) {

    override fun getValue(receipt: Receipt): String = getPrice(receipt).decimalFormattedPrice

    override fun getFooter(receipts: List<Receipt>): String {
        return if (!receipts.isEmpty()) {
            val tripCurrency = receipts[0].trip.tripCurrency
            val prices = ArrayList<Price>()
            for (receipt in receipts) {
                prices.add(getPrice(receipt))
            }

            val total = PriceBuilderFactory().setPrices(prices, tripCurrency).build()

            if (total.currencyCodeCount == 1) total.decimalFormattedPrice else total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }

    private fun getPrice(receipt: Receipt): Price {
        return if (userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)) {
            receipt.price
        } else {
            val factory = PriceBuilderFactory(receipt.price)
            factory.setPrice(receipt.price.price.subtract(receipt.tax.price))
            factory.build()
        }
    }
}
