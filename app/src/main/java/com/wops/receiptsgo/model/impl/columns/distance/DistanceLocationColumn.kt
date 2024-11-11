package com.wops.receiptsgo.model.impl.columns.distance

import android.content.Context

import com.wops.receiptsgo.R
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState

class DistanceLocationColumn(id: Int, syncState: SyncState, private val localizedContext: Context) :
    AbstractColumnImpl<Distance>(
        id,
        DistanceColumnDefinitions.ActualDefinition.LOCATION,
        syncState
    ) {

    override fun getValue(rowItem: Distance): String = rowItem.location

    override fun getFooter(rows: List<Distance>): String =
        localizedContext.getString(R.string.total)

}
