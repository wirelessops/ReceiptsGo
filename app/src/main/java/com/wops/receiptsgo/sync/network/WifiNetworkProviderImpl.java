package com.wops.receiptsgo.sync.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a wifi network. It
 * will also send out broadcasts if we lose/gain this connection. Please note that this class does not check if there is
 * a wifi login page that is blocking actual network access. This can be done by implementing the solution described in
 * the "Handling Network Sign-On" section of this site:
 * http://developer.android.com/reference/java/net/HttpURLConnection.html.
 */
public class WifiNetworkProviderImpl extends AbstractNetworkProvider {

    private final ConnectivityManager mConnectivityManager;

    public WifiNetworkProviderImpl(@NonNull Context context) {
        this(context, (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    public WifiNetworkProviderImpl(@NonNull Context context, @NonNull ConnectivityManager connectivityManager) {
        super(context, ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mConnectivityManager = Preconditions.checkNotNull(connectivityManager);
    }

    @Override
	public synchronized boolean isNetworkAvailable() {
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

}
