package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistancePriceColumn(
    id: Int,
    syncState: SyncState,
    private val allowSpecialCharacters: Boolean
) : AbstractColumnImpl<Distance>(id, DistanceColumnDefinitions.ActualDefinition.PRICE, syncState) {

    override fun getValue(rowItem: Distance): String =
        if (allowSpecialCharacters) rowItem.price.currencyFormattedPrice
        else rowItem.price.currencyCodeFormattedPrice

    override fun getFooter(rows: List<Distance>): String {
        return when {
            rows.isNotEmpty() -> {
                val tripCurrency = rows[0].trip.tripCurrency

                if (allowSpecialCharacters) {
                    PriceBuilderFactory().setPriceables(rows, tripCurrency).build().currencyFormattedPrice
                } else {
                    PriceBuilderFactory().setPriceables(rows, tripCurrency).build().currencyCodeFormattedPrice
                }
            }
            else -> {
                ""
            }
        }
    }
}
