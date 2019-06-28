package co.smartreceipts.android.sync.network;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;

@ApplicationScope
public class NetworkManager extends CompositeNetworkProviderImpl {

    private final NetworkProviderFactory mNetworkProviderFactory;
    private final UserPreferenceManager mPreferences;

    @Inject
    public NetworkManager(Context context, UserPreferenceManager preferences) {
        this(new NetworkProviderFactory(context), preferences);
    }

    private NetworkManager(@NonNull NetworkProviderFactory networkProviderFactory, @NonNull UserPreferenceManager preferences) {
        super(networkProviderFactory.get(preferences.get(UserPreference.Misc.AutoBackupOnWifiOnly) ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks));
        mNetworkProviderFactory = Preconditions.checkNotNull(networkProviderFactory);
        mPreferences = Preconditions.checkNotNull(preferences);
        initialize();
    }

    public synchronized void setAndInitializeNetworkProviderType(@NonNull SupportedNetworkType supportedNetworkType) {
        Logger.info(this, "Updating network provider type to: {}", supportedNetworkType);
        final NetworkProvider newNetworkProvider = mNetworkProviderFactory.get(supportedNetworkType);

        Logger.info(this, "De-initializing the old network type");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.deinitialize();
            networkProvider.unregisterListener(this);
        }

        // Updating
        Logger.info(this, "Updating network type references");
        mNetworkProviders.clear();
        mNetworkProviders.add(newNetworkProvider);

        Logger.info(this, "Initializing the new provider");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.initialize();
            networkProvider.registerListener(this);
        }

        checkForConnectionChange();
    }

    @NonNull
    public synchronized SupportedNetworkType getSupportedNetworkType() {
        return mPreferences.get(UserPreference.Misc.AutoBackupOnWifiOnly) ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks;
    }

}
