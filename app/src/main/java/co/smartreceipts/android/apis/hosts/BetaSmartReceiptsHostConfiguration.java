package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.utils.log.Logger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class BetaSmartReceiptsHostConfiguration extends SmartReceiptsHostConfiguration {

    private final IdentityStore identityStore;

    public BetaSmartReceiptsHostConfiguration(@NonNull IdentityStore identityStore, @NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        super(identityStore, smartReceiptsGsonBuilder);
        this.identityStore = Preconditions.checkNotNull(identityStore);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://beta.smartreceipts.co";
    }

    @NonNull
    @Override
    public OkHttpClient getClient() {
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Logger.info(HttpLoggingInterceptor.class, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return okHttpBuilder()
                .addInterceptor(new SmartReceiptsAuthenticatedRequestInterceptor(identityStore))
                .addInterceptor(new TrafficStatsRequestInterceptor())
                .addInterceptor(loggingInterceptor)
                .build();
    }
}
