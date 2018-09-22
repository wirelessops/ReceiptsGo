package co.smartreceipts.android.aws.cognito;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.apis.me.Cognito;
import dagger.Lazy;

@ApplicationScope
public class LocalCognitoTokenStore {

    private static final String KEY_COGNITO_TOKEN = "key_cognito_token";
    private static final String KEY_COGNITO_IDENTITY_ID = "key_cognito_identity_id";
    private static final String KEY_COGNITO_TOKEN_EXPIRATION = "key_cognito_token_expiration";

    private final Lazy<SharedPreferences> sharedPreferences;

    @Inject
    public LocalCognitoTokenStore(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Nullable
    public Cognito getCognitoToken() {
        final SharedPreferences preferences = sharedPreferences.get();
        final String token = preferences.getString(KEY_COGNITO_TOKEN, null);
        final String identityId = preferences.getString(KEY_COGNITO_IDENTITY_ID, null);
        final long expirationTimeStamp = preferences.getLong(KEY_COGNITO_TOKEN_EXPIRATION, -1);
        if (token == null || identityId == null) {
            return null;
        } else {
            return new Cognito(token, identityId, new Date(expirationTimeStamp));
        }
    }

    public void persist(@Nullable Cognito cognito) {
        final SharedPreferences.Editor editor = sharedPreferences.get().edit();
        editor.putString(KEY_COGNITO_TOKEN, cognito != null ? cognito.getCognitoToken() : null);
        editor.putString(KEY_COGNITO_IDENTITY_ID, cognito != null ? cognito.getIdentityId() : null);
        editor.putLong(KEY_COGNITO_TOKEN_EXPIRATION, cognito != null ? cognito.getCognitoTokenExpiresAt().getTime() : -1);
        editor.apply();
    }

}
