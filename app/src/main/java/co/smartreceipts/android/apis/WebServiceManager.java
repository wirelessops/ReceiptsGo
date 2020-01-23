package co.smartreceipts.android.apis;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import co.smartreceipts.android.apis.hosts.HostConfiguration;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import dagger.Lazy;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Provides a standardized manner in which we can define host configurations and their association to a specific
 * service endpoint for network requests within the app
 */
@ApplicationScope
public class WebServiceManager {

    private final Retrofit retrofit;
    private final Map<Class<?>, Object> cachedServiceMap = new HashMap<>();

    @Inject
    public WebServiceManager(@NonNull HostConfiguration defaultHostConfiguration,
                             @NonNull SmartReceiptsRetrofitConverterFactory smartReceiptsRetrofitConverterFactory,
                             @NonNull Lazy<OkHttpClient> okHttpClientLazy) {

        // Build our retrofit instance
        final Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(defaultHostConfiguration.getBaseUrl());
        builder.callFactory(request -> okHttpClientLazy.get().newCall(request)); // We use a lazy to build this in the background
        builder.addConverterFactory(smartReceiptsRetrofitConverterFactory);
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
    public synchronized <T> T getService(final Class<T> serviceClass) {
        if (cachedServiceMap.containsKey(serviceClass)) {
            return (T) cachedServiceMap.get(serviceClass);
        }

        final T service = retrofit.create(serviceClass);
        cachedServiceMap.put(serviceClass, service);
        return service;
    }
}
