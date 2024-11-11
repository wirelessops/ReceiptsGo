package com.wops.receiptsgo.model.impl.columns

import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.core.sync.model.SyncState
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
