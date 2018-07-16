package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.identity.store.IdentityStore;
import okhttp3.OkHttpClient;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    private final IdentityStore identityStore;
    private final SmartReceiptsGsonBuilder smartReceiptsGsonBuilder;

    public SmartReceiptsHostConfiguration(@NonNull IdentityStore identityStore, @NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        this.identityStore = Preconditions.checkNotNull(identityStore);
        this.smartReceiptsGsonBuilder = Preconditions.checkNotNull(smartReceiptsGsonBuilder);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://www.smartreceipts.co";
    }

    @NonNull
    @Override
    public OkHttpClient getClient() {
        return okHttpBuilder().addInterceptor(new SmartReceiptsAuthenticatedRequestInterceptor(identityStore)).build();
    }

    @NonNull
    @Override
    public final Gson getGson() {
        return smartReceiptsGsonBuilder.create();
    }

    @NonNull
    protected OkHttpClient.Builder okHttpBuilder() {
        return new OkHttpClient.Builder().socketFactory(new TrafficStatsSocketFactory());
    }
}
