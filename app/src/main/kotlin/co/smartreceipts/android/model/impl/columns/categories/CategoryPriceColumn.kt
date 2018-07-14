package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.android.sync.model.SyncState
import java.util.*


class CategoryPriceColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.PRICE,
        syncState
    ) {

    override fun getValue(sumCategoryGroupingResult: SumCategoryGroupingResult): String =
        sumCategoryGroupingResult.price.decimalFormattedPrice

    override fun getFooter(rows: List<SumCategoryGroupingResult>): String {
        return if (!rows.isEmpty()) {
            val tripCurrency = rows[0].currency
            val prices = ArrayList<Price>()
            for (row in rows) {
                prices.add(row.price)
            }

            val total = PriceBuilderFactory().setPrices(prices, tripCurrency).build()

            if (total.currencyCodeCount == 1) total.decimalFormattedPrice else total.currencyCodeFormattedPrice
        } else {
            ""
        }
    }
}
