package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.model.SyncState

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReportStartDateColumn(
    id: Int, syncState: SyncState, private val localizedContext: Context,
    private val preferences: UserPreferenceManager, customOrderId: Long
) : AbstractColumnImpl<Receipt>(
    id,
    ReceiptColumnDefinitions.ActualDefinition.REPORT_START_DATE,
    syncState,
    customOrderId
) {

    override fun getValue(receipt: Receipt): String =
        receipt.trip.getFormattedStartDate(
            localizedContext,
            preferences.get(UserPreference.General.DateSeparator)
        )


    override fun getFooter(rows: List<Receipt>): String =
        if (!rows.isEmpty()) getValue(rows[0]) else ""

}
