package com.wops.receiptsgo.model.impl.columns.distance

import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.core.sync.model.SyncState

class DistanceRateColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<Distance>(id, DistanceColumnDefinitions.ActualDefinition.RATE, syncState) {

    override fun getValue(rowItem: Distance): String = rowItem.decimalFormattedRate

}
