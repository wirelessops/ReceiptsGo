package co.smartreceipts.android.purchases.apis.purchases;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MobileAppPurchasesService {

    @POST("api/mobile_app_purchases")
    Observable<PurchaseResponse> addPurchase(@NonNull @Body PurchaseRequest purchaseRequest);
}
