package co.smartreceipts.android.purchases.plus

import android.content.Context
import co.smartreceipts.android.R
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import wb.android.flex.Flex
import javax.inject.Inject


/**
 * Manages the title string of the application, "Smart Receipts" or "Smart Receipts Plus", to allow
 * us to change the title if the user purchases the plus application
 */
@ApplicationScope
class SmartReceiptsTitle @Inject constructor(private val context: Context,
                                             private val flex: Flex,
                                             private val purchaseWallet: PurchaseWallet) {

    /**
     * Gets the appropriate title for the application, based on if they own the plus purchase
     *
     * @return the title string, "Smart Receipts" or "Smart Receipts Plus"
     */
    fun get() : String {
        return if (purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)) {
            flex.getString(context, R.string.sr_app_name_plus)
        } else {
            flex.getString(context, R.string.sr_app_name)
        }
    }

}