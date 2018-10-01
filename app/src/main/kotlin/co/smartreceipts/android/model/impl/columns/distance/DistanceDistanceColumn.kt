package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.sync.model.SyncState
import java.math.BigDecimal

class DistanceDistanceColumn(id: Int, syncState: SyncState) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.DISTANCE,
    syncState
) {

    override fun getValue(distance: Distance): String = distance.decimalFormattedDistance

    override fun getFooter(distances: List<Distance>): String {
        var distance = BigDecimal.ZERO
        for (i in distances.indices) {
            distance = distance.add(distances[i].distance)
        }
        return ModelUtils.getDecimalFormattedValue(distance)
    }
}
