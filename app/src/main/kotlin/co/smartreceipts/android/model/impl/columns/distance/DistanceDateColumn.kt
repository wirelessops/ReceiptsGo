package co.smartreceipts.android.model.impl.columns.distance

import android.content.Context

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.model.SyncState

class DistanceDateColumn(
    id: Int, syncState: SyncState, private val localizedContext: Context,
    private val preferences: UserPreferenceManager
) : AbstractColumnImpl<Distance>(id, DistanceColumnDefinitions.ActualDefinition.DATE, syncState) {

    override fun getValue(distance: Distance): String =
        distance.getFormattedDate(
            localizedContext,
            preferences.get(UserPreference.General.DateSeparator)
        )
}
