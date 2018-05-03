package co.smartreceipts.android.identity.store;

import android.support.annotation.Nullable;

public interface IdentityStore {

    /**
     * @return the user's {@link EmailAddress} or {@code null} if the user is not currently signed in
     */
    @Nullable
    EmailAddress getEmail();

    /**
     * @return the user's {@link UserId} or {@code null} if the user is not currently signed in
     */
    @Nullable
    UserId getUserId();

    /**
     * @return the user's {@link Token} or {@code null} if the user is not currently signed in
     */
    @Nullable
    Token getToken();

    /**
     * @return {@code true} if the user is signed in. {@code false} otherwise
     */
    boolean isLoggedIn();

}
