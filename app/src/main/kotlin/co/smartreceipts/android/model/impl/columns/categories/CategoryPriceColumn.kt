package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.android.sync.model.SyncState


class CategoryPriceColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.PRICE,
        syncState
    ) {

    override fun getValue(sumCategoryGroupingResult: SumCategoryGroupingResult): String {
        return sumCategoryGroupingResult.netPrice.currencyCodeFormattedPrice
    }

    override fun getFooter(rows: List<SumCategoryGroupingResult>): String {
        return if (!rows.isEmpty()) {
            val prices = ArrayList<Price>()

            for (row in rows) {
                for (entry in row.netPrice.immutableOriginalPrices.entries) {
                    prices.add(PriceBuilderFactory().setCurrency(entry.key).setPrice(entry.value).build())
                }
            }

            val total = PriceBuilderFactory().setPrices(prices, rows[0].baseCurrency).build()

            total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }
}
