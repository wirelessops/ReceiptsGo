package co.smartreceipts.android.purchases.apis.subscriptions

import io.reactivex.Observable
import retrofit2.http.GET

interface SubscriptionsApiService {

    @GET("api/subscriptions")
    fun addPurchase(): Observable<SubscriptionsApiResponse>
}
