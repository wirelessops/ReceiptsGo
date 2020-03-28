package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistanceCurrencyColumn(id: Int, syncState: SyncState) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.CURRENCY,
    syncState
) {

    override fun getValue(rowItem: Distance): String = rowItem.price.currencyCode

    override fun getFooter(rows: List<Distance>): String {
        val tripCurrency = rows[0].trip.tripCurrency
        return PriceBuilderFactory().setPriceables(rows, tripCurrency).build().currencyCode
    }
}
