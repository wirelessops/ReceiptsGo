package com.wops.receiptsgo.model.impl.columns.categories

import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

class CategoryExchangedPriceColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.PRICE_EXCHANGED,
        syncState
    ) {

    override fun getValue(rowItem: SumCategoryGroupingResult): String {
        val price = rowItem.netPrice
        return price.currency.code + price.decimalFormattedPrice
    }

    override fun getFooter(rows: List<SumCategoryGroupingResult>): String {
        return if (rows.isNotEmpty()) {
            val tripCurrency = rows[0].baseCurrency
            val prices = ArrayList<Price>()
            for (row in rows) {
                prices.add(row.netPrice)
            }

            val total = PriceBuilderFactory().setPrices(prices, tripCurrency).build()

            total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }
}
