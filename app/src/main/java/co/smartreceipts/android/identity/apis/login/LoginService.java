package co.smartreceipts.android.identity.apis.login;

import android.support.annotation.NonNull;

import co.smartreceipts.android.apis.SmartReceiptsRetrofitConverterFactory;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginService {

    @POST("api/users/log_in")
    @SmartReceiptsRetrofitConverterFactory.GsonType
    Observable<LoginResponse> logIn(@NonNull @Body LoginPayload loginPayload);
}
