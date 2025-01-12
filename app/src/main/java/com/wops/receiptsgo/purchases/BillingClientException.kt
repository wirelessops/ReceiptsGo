package com.wops.receiptsgo.purchases

class BillingClientException(
    val responseCode: Int,
    message: String
) : Exception("ResponseCode $responseCode, message: $message")