package co.smartreceipts.android.identity.apis.logout;

import io.reactivex.Observable;
import retrofit2.http.POST;

public interface LogoutService {

    @POST("api/users/log_out")
    Observable<LogoutResponse> logOut();
}
