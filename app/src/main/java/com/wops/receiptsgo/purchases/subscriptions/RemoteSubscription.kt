package com.wops.receiptsgo.purchases.subscriptions

import com.wops.receiptsgo.purchases.model.InAppPurchase
import java.util.*

data class RemoteSubscription(val id: Long,
                              val inAppPurchase: InAppPurchase,
                              val expirationDate: Date)