package com.wops.receiptsgo.model.impl.columns.distance

import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.model.utils.ModelUtils
import com.wops.core.sync.model.SyncState
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
