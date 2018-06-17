package co.smartreceipts.android.aws.cognito;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import co.smartreceipts.android.identity.apis.me.Cognito;

public class LocalCognitoTokenStore {

    private static final String KEY_COGNITO_TOKEN = "key_cognito_token";
    private static final String KEY_COGNITO_IDENTITY_ID = "key_cognito_identity_id";
    private static final String KEY_COGNITO_TOKEN_EXPIRATION = "key_cognito_token_expiration";

    private final SharedPreferences mSharedPreferences;

    public LocalCognitoTokenStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @Inject
    public LocalCognitoTokenStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    @Nullable
    public Cognito getCognitoToken() {
        final String token = mSharedPreferences.getString(KEY_COGNITO_TOKEN, null);
        final String identityId = mSharedPreferences.getString(KEY_COGNITO_IDENTITY_ID, null);
        final long expirationTimeStamp = mSharedPreferences.getLong(KEY_COGNITO_TOKEN_EXPIRATION, -1);
        if (token == null || identityId == null) {
            return null;
        } else {
            return new Cognito(token, identityId, expirationTimeStamp);
        }
    }

    public void persist(@Nullable Cognito cognito) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_COGNITO_TOKEN, cognito != null ? cognito.getCognitoToken() : null);
        editor.putString(KEY_COGNITO_IDENTITY_ID, cognito != null ? cognito.getIdentityId() : null);
        editor.putLong(KEY_COGNITO_TOKEN_EXPIRATION, cognito != null ? cognito.getCognitoTokenExpiresAt() : -1);
        editor.apply();
    }

}
