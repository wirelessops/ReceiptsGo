package co.smartreceipts.android.model.impl.columns

import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns blank values for everything but the header
 */
class SettingUserIdColumn<T>(
    id: Int, syncState: SyncState,
    private val preferences: UserPreferenceManager, customOrderId: Long
) : AbstractColumnImpl<T>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.USER_ID,
    syncState,
    customOrderId
) {

    override fun getValue(rowItem: T): String = preferences.get(UserPreference.ReportOutput.UserId)
}
