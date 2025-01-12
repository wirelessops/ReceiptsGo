package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context
import com.wops.receiptsgo.R
import com.wops.receiptsgo.model.ActualColumnDefinition
import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.model.utils.ModelUtils
import com.wops.core.sync.model.SyncState
import java.util.*

/**
 * Allows us to genericize how different prices are converted to a trip's base currency
 */
abstract class AbstractExchangedPriceColumn(
    id: Int,
    definition: ActualColumnDefinition,
    syncState: SyncState,
    private val localizedContext: Context,
    customOrderId: Long,
    uuid: UUID
) : AbstractColumnImpl<Receipt>(id, definition, syncState, customOrderId, uuid) {

    override fun getValue(rowItem: Receipt): String {
        val price = getPrice(rowItem)
        val exchangeRate = price.exchangeRate
        val baseCurrency = rowItem.trip.tripCurrency

        return if (exchangeRate.supportsExchangeRateFor(baseCurrency)) {
            ModelUtils.getDecimalFormattedValue(price.price.multiply(exchangeRate.getExchangeRate(baseCurrency)), baseCurrency.decimalPlaces)
        } else {
            localizedContext.getString(R.string.undefined)
        }
    }

    override fun getFooter(rows: List<Receipt>): String {
        return if (rows.isNotEmpty()) {
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
