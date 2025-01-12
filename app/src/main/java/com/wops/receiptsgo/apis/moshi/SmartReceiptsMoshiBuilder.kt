package com.wops.receiptsgo.apis.moshi

import com.wops.receiptsgo.apis.moshi.adapters.*
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.*
import javax.inject.Inject

class SmartReceiptsMoshiBuilder @Inject constructor(val receiptColumnDefinitions: ReceiptColumnDefinitions) {

    fun create(): Moshi {
        return Moshi.Builder()
            .add(CategoryJsonAdapter())
            .add(PaymentMethodJsonAdapter())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(UserRoleJsonAdapter())
            .add(PreferenceJsonAdapter())
            .add(ColumnJsonAdapter(receiptColumnDefinitions))
            .build()
    }
}