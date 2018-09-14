package co.smartreceipts.android.apis.hosts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.apis.SmartReceiptsApisRxJavaCallAdapterFactory;
import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a standardized manner in which we can define host configurations and their association to a specific
 * service endpoint for network requests within the app
 */
@ApplicationScope
public class WebServiceManager {

    private final static int CACHE_SIZE_BYTES = 1024 * 1024 * 5; // 5MB

    private final Retrofit retrofit;
    private final Map<Class<?>, Object> cachedServiceMap = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public WebServiceManager(@NonNull Context context,
                             @NonNull HostConfiguration defaultHostConfiguration,
                             @NonNull IdentityStore identityStore,
                             @NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {

        Preconditions.checkNotNull(defaultHostConfiguration);
        Preconditions.checkNotNull(identityStore);
        Preconditions.checkNotNull(smartReceiptsGsonBuilder);

        // Configure our baseline OkHttp client
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new SmartReceiptsAuthenticatedRequestInterceptor(identityStore));
        okHttpClientBuilder.addInterceptor(new TrafficStatsRequestInterceptor());
        if (BuildConfig.DEBUG) {
            // If we're using a debug build, add logging
            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Logger.debug(HttpLoggingInterceptor.class, message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        okHttpClientBuilder.cache(new Cache(context.getCacheDir(), CACHE_SIZE_BYTES));

        // Build our retrofit instance
        final Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(defaultHostConfiguration.getBaseUrl());
        builder.client(okHttpClientBuilder.build());
        builder.addConverterFactory(GsonConverterFactory.create(smartReceiptsGsonBuilder.create()));
        builder.addCallAdapterFactory(SmartReceiptsApisRxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()));

        retrofit = builder.build();
    }

    /**
     * Generates an appropriate service that can be used for network requests
     *
     * @param serviceClass the service class type
     * @return an instance of the service class, which can be used for the actual request
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public synchronized  <T> T getService(final Class<T> serviceClass) {
        if (cachedServiceMap.containsKey(serviceClass)) {
            return (T) cachedServiceMap.get(serviceClass);
        }

        final T service = retrofit.create(serviceClass);
        cachedServiceMap.put(serviceClass, service);
        return service;
    }
}
