package co.smartreceipts.android.config;

import android.support.annotation.NonNull;

import co.smartreceipts.android.utils.Feature;

/**
 * Provides a top level mechanism from which we can easily toggle on/off certain components within the app
 * in order that we might better support certain "white-label" efforts for our clients. This is defined in
 * the form of a contract interface to enable easy over-riding if required
 */
public interface ConfigurationManager {

    /**
     * Checks if this particular feature is enabled
     *
     * @return {@code true} if it is enabled. {@code false} otherwise
     */
    boolean isEnabled(@NonNull Feature feature);

}
