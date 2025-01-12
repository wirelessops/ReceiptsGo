package com.wops.receiptsgo.sync.network;

/**
 * This listener should be overridden in order to identity when network state has been either gained or lost
 * 
 * @author Will Baumann
 */
public interface NetworkStateChangeListener {

	/**
	 * This method is called whenever the network connection is lost. It will also be called when this listener is first
	 * registered if network is not currently available.
	 */
	void onNetworkConnectivityLost();

	/**
	 * This method is called whenever the network connection is regained. It will also be called when this listener is
	 * first registered if network is currently available.
	 */
	void onNetworkConnectivityGained();

}
