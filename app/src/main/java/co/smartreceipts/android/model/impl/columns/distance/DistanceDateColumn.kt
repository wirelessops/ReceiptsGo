package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistanceDateColumn(
    id: Int, syncState: SyncState, private val dateFormatter: DateFormatter
) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.DATE,
    syncState
) {

    override fun getValue(rowItem: Distance): String = dateFormatter.getFormattedDate(rowItem.displayableDate)
}
