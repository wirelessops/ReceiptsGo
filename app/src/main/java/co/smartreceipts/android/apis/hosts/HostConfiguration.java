package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

/**
 * A simple interface that allows us to toggle between the production and beta URLs/configurations
 */
public interface HostConfiguration {

    /**
     * @return a string pointing to the web host (e.g. "https://smartreceipts.co")
     */
    @NonNull
    String getBaseUrl();

}
