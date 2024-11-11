package com.wops.receiptsgo.purchases.apis.subscriptions

import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Provides a simple interface with which we can interface with our remote subscriptions API
 */
interface SubscriptionsApiService {

    @GET("api/subscriptions")
    fun getSubscriptions(): Observable<SubscriptionsApiResponse>
}
