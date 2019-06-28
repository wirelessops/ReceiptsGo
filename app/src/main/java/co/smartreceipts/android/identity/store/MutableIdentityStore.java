package co.smartreceipts.android.identity.store;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Lazy;

@ApplicationScope
public final class MutableIdentityStore implements IdentityStore {

    private static final String KEY_EMAIL = "identity_email_address";
    private static final String KEY_USER_ID = "identity_user_id";
    private static final String KEY_TOKEN = "identity_token";

    private final Lazy<SharedPreferences> sharedPreferences;

    @Inject
    public MutableIdentityStore(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Nullable
    @Override
    public EmailAddress getEmail() {
        final String email = sharedPreferences.get().getString(KEY_EMAIL, null);
        if (email != null) {
            return new EmailAddress(email);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public UserId getUserId() {
        final String userId = sharedPreferences.get().getString(KEY_USER_ID, null);
        if (userId != null) {
            return new UserId(userId);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Token getToken() {
        final String token = sharedPreferences.get().getString(KEY_TOKEN, null);
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
        final SharedPreferences.Editor editor = sharedPreferences.get().edit();
        editor.putString(KEY_EMAIL, emailAddress);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    @Override
    public void logOut() {
        final SharedPreferences.Editor editor = sharedPreferences.get().edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
