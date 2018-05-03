package co.smartreceipts.android.identity.apis.login;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private String token;

    private String id;

    @Nullable
    public String getToken() {
        return token;
    }

    @Nullable
    public String getId() {
        return id;
    }
}
