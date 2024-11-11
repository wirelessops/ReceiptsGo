package com.wops.receiptsgo.model.impl.columns.distance

import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
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
