package co.smartreceipts.android.identity.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public final class MutableIdentityStore implements IdentityStore {

    private static final String KEY_EMAIL = "identity_email_address";
    private static final String KEY_USER_ID = "identity_user_id";
    private static final String KEY_TOKEN = "identity_token";

    private final SharedPreferences sharedPreferences;

    @Inject
    public MutableIdentityStore(Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    public MutableIdentityStore(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Nullable
    @Override
    public EmailAddress getEmail() {
        final String email = sharedPreferences.getString(KEY_EMAIL, null);
        if (email != null) {
            return new EmailAddress(email);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public UserId getUserId() {
        final String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null) {
            return new UserId(userId);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Token getToken() {
        final String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (token != null) {
            return new Token(token);
        } else {
            return null;
        }
    }

    @Override
    public boolean isLoggedIn() {
        return (getEmail() != null || getUserId() != null) && getToken() != null;
    }

    public void setCredentials(@Nullable String emailAddress, @Nullable String userId, @Nullable String token) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, emailAddress);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

}
