package co.smartreceipts.android.model.impl.columns

import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns blank values for everything but the header
 */
class SettingUserIdColumn<T>(
    id: Int, syncState: SyncState,
    private val preferences: UserPreferenceManager, customOrderId: Long, uuid: UUID
) : AbstractColumnImpl<T>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.USER_ID,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(rowItem: T): String = preferences.get(UserPreference.ReportOutput.UserId)
}
