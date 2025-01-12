package com.wops.receiptsgo.identity.apis.signup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import com.wops.core.identity.apis.login.UserCredentialsPayload;

public class SignUpPayload implements Serializable {

    @SerializedName("signup_params")
    private final UserCredentialsPayload signUpParams;

    public SignUpPayload(@NonNull UserCredentialsPayload signUpParams) {
        this.signUpParams = signUpParams;
    }

    @Nullable
    public UserCredentialsPayload getSignUpParams() {
        return signUpParams;
    }
}