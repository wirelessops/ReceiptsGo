package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.purchases.model.InAppPurchase
import java.util.*

data class RemoteSubscription(val id: Long,
                              val inAppPurchase: InAppPurchase,
                              val expirationDate: Date)