package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.purchases.model.PurchaseFamily
import java.util.*

data class RemoteSubscription(val id: Int,
                              val purchaseFamily: PurchaseFamily?,
                              val expirationDate: Date)