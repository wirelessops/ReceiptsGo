package com.wops.receiptsgo.model.impl.columns.distance

import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
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
