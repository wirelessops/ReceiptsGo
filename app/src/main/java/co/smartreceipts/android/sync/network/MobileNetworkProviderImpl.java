package co.smartreceipts.android.sync.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a mobile network. It
 * will also send out broadcasts if we lose/gain this connection.
 */
public class MobileNetworkProviderImpl extends AbstractNetworkProvider {

	private final ConnectivityManager mConnectivityManager;

	public MobileNetworkProviderImpl(@NonNull Context context) {
        this(context, (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

    public MobileNetworkProviderImpl(@NonNull Context context, @NonNull ConnectivityManager connectivityManager) {
        super(context, ConnectivityManager.CONNECTIVITY_ACTION);
        mConnectivityManager = Preconditions.checkNotNull(connectivityManager);
    }

    @Override
	public boolean isNetworkAvailable() {
	    final NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (networkInfo != null) {
	        return networkInfo.isConnected();
        } else {
	        return false;
        }
	}

}
