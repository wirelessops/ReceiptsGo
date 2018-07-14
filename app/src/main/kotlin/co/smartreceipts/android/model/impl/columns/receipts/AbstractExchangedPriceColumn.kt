package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context
import co.smartreceipts.android.R
import co.smartreceipts.android.model.ActualColumnDefinition
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Allows us to genericize how different prices are converted to a trip's base currency
 */
abstract class AbstractExchangedPriceColumn(
    id: Int, definition: ActualColumnDefinition,
    syncState: SyncState,
    private val localizedContext: Context, customOrderId: Long
) : AbstractColumnImpl<Receipt>(id, definition, syncState, customOrderId) {

    override fun getValue(receipt: Receipt): String? {
        val price = getPrice(receipt)
        val exchangeRate = price.exchangeRate
        val baseCurrency = receipt.trip.tripCurrency
        return if (exchangeRate.supportsExchangeRateFor(baseCurrency)) {
            ModelUtils.getDecimalFormattedValue(
                price.price.multiply(
                    exchangeRate.getExchangeRate(
                        baseCurrency
                    )
                )
            )
        } else {
            localizedContext.getString(R.string.undefined)
        }
    }

    override fun getFooter(rows: List<Receipt>): String {
        return if (!rows.isEmpty()) {
            val factory = PriceBuilderFactory()
            val prices = ArrayList<Price>(rows.size)
            for (receipt in rows) {
                factory.setCurrency(receipt.trip.tripCurrency)
                prices.add(getPrice(receipt))
            }
            factory.setPrices(prices, rows[0].trip.tripCurrency)
            factory.build().decimalFormattedPrice
        } else {
            ""
        }
    }

    protected abstract fun getPrice(receipt: Receipt): Price
}
