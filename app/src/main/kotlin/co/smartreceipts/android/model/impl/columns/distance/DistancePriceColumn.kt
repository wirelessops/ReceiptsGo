package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

class DistancePriceColumn(
    id: Int,
    syncState: SyncState,
    private val allowSpecialCharacters: Boolean
) : AbstractColumnImpl<Distance>(id, DistanceColumnDefinitions.ActualDefinition.PRICE, syncState) {

    override fun getValue(distance: Distance): String =
        if (allowSpecialCharacters) distance.price.currencyFormattedPrice
        else distance.price.currencyCodeFormattedPrice

    override fun getFooter(distances: List<Distance>): String {
        val tripCurrency = if (!distances.isEmpty()) distances[0].trip.tripCurrency else null
        return if (allowSpecialCharacters) {
            PriceBuilderFactory().setPriceables(distances, tripCurrency!!).build().currencyFormattedPrice
        } else {
            PriceBuilderFactory().setPriceables(distances, tripCurrency!!).build().currencyCodeFormattedPrice
        }
    }
}
