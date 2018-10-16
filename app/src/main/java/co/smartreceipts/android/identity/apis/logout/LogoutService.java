package co.smartreceipts.android.identity.apis.logout;

import co.smartreceipts.android.apis.SmartReceiptsRetrofitConverterFactory;
import io.reactivex.Observable;
import retrofit2.http.POST;

public interface LogoutService {

    @POST("api/users/log_out")
    @SmartReceiptsRetrofitConverterFactory.GsonType
    Observable<LogoutResponse> logOut();
}
