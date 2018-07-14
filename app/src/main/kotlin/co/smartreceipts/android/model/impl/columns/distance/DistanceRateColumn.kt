package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

class DistanceRateColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<Distance>(id, DistanceColumnDefinitions.ActualDefinition.RATE, syncState) {

    override fun getValue(distance: Distance): String? = distance.decimalFormattedRate

}
