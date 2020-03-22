package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistanceCommentColumn(id: Int, syncState: SyncState) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.COMMENT,
    syncState
) {

    override fun getValue(rowItem: Distance): String = rowItem.comment
}
