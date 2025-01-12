package com.wops.aws.cognito;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Cognito implements Serializable {

    private final String cognito_token;
    private final String identity_id;
    private final Date cognito_token_expires_at;

    public Cognito(@Nullable String cognito_token, @Nullable String identity_id, Date cognito_token_expires_at) {
        this.cognito_token = cognito_token;
        this.identity_id = identity_id;
        this.cognito_token_expires_at = cognito_token_expires_at;
    }

    @Nullable
    public String getCognitoToken() {
        return cognito_token;
    }

    @Nullable
    public String getIdentityId() {
        return this.identity_id;
    }

    public Date getCognitoTokenExpiresAt() {
        return cognito_token_expires_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cognito)) return false;
        Cognito cognito = (Cognito) o;
        return Objects.equals(cognito_token, cognito.cognito_token) &&
                Objects.equals(identity_id, cognito.identity_id) &&
                Objects.equals(cognito_token_expires_at, cognito.cognito_token_expires_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cognito_token, identity_id, cognito_token_expires_at);
    }
}
