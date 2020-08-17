package co.smartreceipts.android.model.impl.columns.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.core.sync.model.SyncState
import java.math.BigDecimal

class DistanceDistanceColumn(id: Int, syncState: SyncState) : AbstractColumnImpl<Distance>(
    id,
    DistanceColumnDefinitions.ActualDefinition.DISTANCE,
    syncState
) {

    override fun getValue(rowItem: Distance): String = rowItem.decimalFormattedDistance

    override fun getFooter(rows: List<Distance>): String {
        var distance = BigDecimal.ZERO
        for (i in rows.indices) {
            distance = distance.add(rows[i].distance)
        }
        return ModelUtils.getDecimalFormattedValue(distance, Distance.DISTANCE_PRECISION)
    }
}
