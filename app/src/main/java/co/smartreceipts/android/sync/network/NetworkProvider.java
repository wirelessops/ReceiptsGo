package co.smartreceipts.android.sync.network;

import androidx.annotation.NonNull;

/**
 * This interface defines different network provider types (e.g. WiFi-Only vs Mobile) in order to allow us to track the
 * current state and changing states of each
 */
public interface NetworkProvider {

    void initialize();

    void deinitialize();

	/**
	 * Checks if a network connection is currently available
	 * 
	 * @return {@code true} if the network is currently available. {@code false} if it is not.
	 */
	boolean isNetworkAvailable();

    /**
     * Registers a listener to be informed network is lost or gained
     *
     * @param listener the {@link NetworkStateChangeListener} to register
     */
    void registerListener(@NonNull NetworkStateChangeListener listener);

    /**
     * Un-registers a listener to no longer be if informed network is lost or gained
     *
     * @param listener the {@link NetworkStateChangeListener} to un-register
     */
    void unregisterListener(@NonNull NetworkStateChangeListener listener);

}
