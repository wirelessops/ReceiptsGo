package com.wops.receiptsgo.purchases.apis.subscriptions

import java.util.*

data class Subscription(val id: Long?,
                        val subscription_provider: String?,
                        val product_name: String?,
                        val purchased_at: Date?,
                        val expires_at_iso8601: Date?)