package com.wops.receiptsgo.model.impl.columns.receipts

import android.content.Context
import com.wops.receiptsgo.model.Price
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.core.sync.model.SyncState
import java.util.*

/**
 * Provides a column that returns the total of the price and tax fields based on user settings
 */
class ReceiptNetExchangedPriceMinusTaxColumn(
    id: Int, syncState: SyncState,
    localizedContext: Context,
    private val userPreferenceManager: UserPreferenceManager,
    customOrderId: Long,
    uuid: UUID
) : AbstractExchangedPriceColumn(
    id,
    ReceiptColumnDefinitions.ActualDefinition.PRICE_MINUS_TAX_EXCHANGED,
    syncState,
    localizedContext,
    customOrderId,
    uuid
) {

    override fun getPrice(receipt: Receipt): Price {
        return if (userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)) {
            receipt.price
        } else {
            PriceBuilderFactory(receipt.price)
                .setPrice(receipt.price.price.subtract(receipt.tax.price).subtract(receipt.tax2.price))
                .build()
        }
    }
}
