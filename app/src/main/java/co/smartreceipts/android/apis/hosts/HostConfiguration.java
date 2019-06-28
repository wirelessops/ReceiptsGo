package co.smartreceipts.android.apis.hosts;

import androidx.annotation.NonNull;

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
