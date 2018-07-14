package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.android.sync.model.SyncState
import java.util.*


class CategoryCurrencyColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.CURRENCY,
        syncState
    ) {

    override fun getValue(sumCategoryGroupingResult: SumCategoryGroupingResult): String =
        sumCategoryGroupingResult.currency.currencyCode

    override fun getFooter(rows: List<SumCategoryGroupingResult>): String {
        return if (!rows.isEmpty()) {
            val tripCurrency = rows[0].currency
            val prices = ArrayList<Price>()
            for (row in rows) {
                prices.add(row.price)
            }
            PriceBuilderFactory().setPrices(prices, tripCurrency).build().currencyCode
        } else {
            ""
        }
    }

}
