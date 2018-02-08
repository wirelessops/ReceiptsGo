package co.smartreceipts.android.di;


import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.apis.hosts.BetaSmartReceiptsHostConfiguration;
import co.smartreceipts.android.apis.hosts.HostConfiguration;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.hosts.SmartReceiptsHostConfiguration;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.utils.FeatureFlags;
import co.smartreceipts.android.utils.log.Logger;
import dagger.Module;
import dagger.Provides;

@Module
public class NetworkingModule {

    @Provides
    @ApplicationScope
    public static SmartReceiptsGsonBuilder provideGson(ReceiptColumnDefinitions receiptColumnDefinitions) {
        return new SmartReceiptsGsonBuilder(receiptColumnDefinitions);
    }

    @Provides
    @ApplicationScope
    public static ServiceManager provideServiceManager(MutableIdentityStore mutableIdentityStore,
                                                       SmartReceiptsGsonBuilder gsonBuilder) {
        final HostConfiguration host;
        if (FeatureFlags.UseProductionEndpoint.isEnabled()) {
            host = new SmartReceiptsHostConfiguration(mutableIdentityStore, gsonBuilder);
        } else {
            Logger.warn(BaseAppModule.class, "*****Configuring our app to use our beta endpoint*****");
            host = new BetaSmartReceiptsHostConfiguration(mutableIdentityStore, gsonBuilder);
        }
        return new ServiceManager(host);
    }


}
