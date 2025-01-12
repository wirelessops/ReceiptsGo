package com.wops.receiptsgo.model.impl.columns.categories

import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import com.wops.core.sync.model.SyncState
import java.util.*

class CategoryTaxColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.TAX,
        syncState
    ) {

    override fun getValue(rowItem: SumCategoryGroupingResult): String =
        rowItem.netTax.currencyCodeFormattedPrice

    override fun getFooter(rows: List<SumCategoryGroupingResult>): String {
        return if (rows.isNotEmpty()) {
            val prices = ArrayList<Price>()

            for (row in rows) {
                for (entry in row.netTax.immutableOriginalPrices.entries) {
                    prices.add(PriceBuilderFactory().setCurrency(entry.key).setPrice(entry.value.amount).build())
                }
            }

            val total = PriceBuilderFactory().setPrices(prices, rows[0].baseCurrency).build()

            total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }
}
