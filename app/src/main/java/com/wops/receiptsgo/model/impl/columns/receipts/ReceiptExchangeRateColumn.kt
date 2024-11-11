package com.wops.receiptsgo.model.impl.columns.receipts

import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the category code for a particular receipt
 */
class ReceiptExchangeRateColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.EXCHANGE_RATE,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String =
        rowItem.price.exchangeRate.getDecimalFormattedExchangeRate(rowItem.trip.tripCurrency)
}
