package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptDateColumn(
    id: Int, syncState: SyncState, private val localizedContext: Context,
    private val preferences: UserPreferenceManager, customOrderId: Long, uuid: UUID
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.DATE,
    syncState,
    customOrderId,
    uuid
) {

    override fun getValue(rowItem: Receipt): String =
        rowItem.getFormattedDate(
            localizedContext,
            preferences.get(UserPreference.General.DateSeparator)
        )
}
