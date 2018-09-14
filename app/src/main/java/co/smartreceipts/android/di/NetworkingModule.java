package co.smartreceipts.android.di;


import android.content.Context;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.apis.hosts.BetaSmartReceiptsHostConfiguration;
import co.smartreceipts.android.apis.hosts.HostConfiguration;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.apis.hosts.SmartReceiptsHostConfiguration;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.utils.ConfigurableStaticFeature;
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
    public static WebServiceManager provideServiceManager(MutableIdentityStore mutableIdentityStore,
                                                          SmartReceiptsGsonBuilder gsonBuilder,
                                                          Context context) {
        final HostConfiguration host;
        if (ConfigurableStaticFeature.UseProductionEndpoint.isEnabled(context)) {
            host = new SmartReceiptsHostConfiguration();
        } else {
            Logger.warn(BaseAppModule.class, "*****Configuring our app to use our beta endpoint*****");
            host = new BetaSmartReceiptsHostConfiguration();
        }
        return new WebServiceManager(context, host, mutableIdentityStore, gsonBuilder);
    }


}
