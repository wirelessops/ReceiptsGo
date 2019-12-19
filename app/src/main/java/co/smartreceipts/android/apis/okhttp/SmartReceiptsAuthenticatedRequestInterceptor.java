package co.smartreceipts.android.apis.okhttp;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.IOException;

import co.smartreceipts.core.identity.store.IdentityStore;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Automatically applied our API Authentication Token to requests, assuming that the user is logged in.
 * If the user is not logged in, then nothing happens.
 */
public class SmartReceiptsAuthenticatedRequestInterceptor implements Interceptor {

    private final IdentityStore identityStore;

    public SmartReceiptsAuthenticatedRequestInterceptor(@NonNull IdentityStore identityStore) {
        this.identityStore = Preconditions.checkNotNull(identityStore);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        final HttpUrl.Builder builder = request.url().newBuilder();
        if (identityStore.isLoggedIn()) {
            if (identityStore.getUserId() != null) {
                builder.addQueryParameter("auth_params[id]", identityStore.getUserId().getId());
            } else {
                // Legacy route for authentication
                builder.addQueryParameter("auth_params[email]", identityStore.getEmail().getId());
            }
            builder.addQueryParameter("auth_params[token]", identityStore.getToken().getId());
        }
        return chain.proceed(request.newBuilder().url(builder.build()).build());
    }
}
