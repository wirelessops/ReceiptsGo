package co.smartreceipts.android.sync.network;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class NetworkProviderFactory {

    private final Context mContext;

    public NetworkProviderFactory(@NonNull Context context) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
    }

    @NonNull
    public NetworkProvider get(@NonNull SupportedNetworkType supportedNetworkType) {
        if (supportedNetworkType == SupportedNetworkType.WifiOnly) {
            return new WifiNetworkProviderImpl(mContext);
        } else {
            return new CompositeNetworkProviderImpl(new WifiNetworkProviderImpl(mContext), new MobileNetworkProviderImpl(mContext));
        }
    }
}
