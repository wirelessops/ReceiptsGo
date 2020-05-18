package co.smartreceipts.android.model.impl.columns.receipts

import android.content.Context

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.core.sync.model.SyncState
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
            val factory = PriceBuilderFactory(receipt.price)
            factory.setPrice(receipt.price.price.subtract(receipt.tax.price).subtract(receipt.tax2.price))
            factory.build()
        }
    }
}
