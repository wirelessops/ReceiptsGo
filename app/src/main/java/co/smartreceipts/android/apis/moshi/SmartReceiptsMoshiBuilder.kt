package co.smartreceipts.android.apis.moshi

import co.smartreceipts.android.apis.moshi.adapters.CategoryJsonAdapter
import co.smartreceipts.android.apis.moshi.adapters.PaymentMethodJsonAdapter
import co.smartreceipts.android.apis.moshi.adapters.UserRoleJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.*
import javax.inject.Inject

class SmartReceiptsMoshiBuilder @Inject constructor() {

    fun create(): Moshi {
        return Moshi.Builder()
            .add(CategoryJsonAdapter())
            .add(PaymentMethodJsonAdapter())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(UserRoleJsonAdapter())
            .build()
    }
}