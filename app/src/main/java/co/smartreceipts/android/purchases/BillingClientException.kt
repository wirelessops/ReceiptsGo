package co.smartreceipts.android.purchases

class BillingClientException(
    val responseCode: Int,
    message: String
) : RuntimeException(message)