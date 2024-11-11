package com.wops.receiptsgo.model.impl.columns.distance

import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistanceCommentColumn(id: Int, syncState: SyncState) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.COMMENT,
    syncState
) {

    override fun getValue(rowItem: Distance): String = rowItem.comment
}
