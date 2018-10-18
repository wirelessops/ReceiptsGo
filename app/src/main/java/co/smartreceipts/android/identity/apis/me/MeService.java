package co.smartreceipts.android.identity.apis.me;

import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface MeService {

    @GET("api/users/me")
    Observable<MeResponse> me();

    @PATCH("api/users/me")
    Observable<MeResponse> me(@Body UpdatePushTokensRequest request);

}
