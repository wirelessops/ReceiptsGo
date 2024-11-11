package com.wops.receiptsgo.identity.apis.login;

import androidx.annotation.NonNull;

import co.smartreceipts.core.identity.apis.login.LoginPayload;
import co.smartreceipts.core.identity.apis.login.LoginResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginService {

    @POST("api/users/log_in")
    Observable<LoginResponse> logIn(@NonNull @Body LoginPayload loginPayload);
}
