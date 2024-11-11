package com.wops.receiptsgo.di;


import android.content.Context;

import com.wops.receiptsgo.apis.gson.SmartReceiptsGsonBuilder;
import com.wops.receiptsgo.apis.hosts.BetaSmartReceiptsHostConfiguration;
import com.wops.receiptsgo.apis.hosts.HostConfiguration;
import com.wops.receiptsgo.apis.hosts.SmartReceiptsHostConfiguration;
import com.wops.receiptsgo.apis.okhttp.SmartReceiptsOkHttpClientFactory;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.core.identity.store.MutableIdentityStore;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.utils.ConfigurableStaticFeature;
import com.wops.analytics.log.Logger;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class NetworkingModule {

    @Provides
    @ApplicationScope
    public static SmartReceiptsGsonBuilder provideGson(ReceiptColumnDefinitions receiptColumnDefinitions) {
        return new SmartReceiptsGsonBuilder(receiptColumnDefinitions);
    }

    @Provides
    @ApplicationScope
    public static HostConfiguration provideHostConfiguration(Context context) {
        if (ConfigurableStaticFeature.UseProductionEndpoint.isEnabled(context)) {
            return new SmartReceiptsHostConfiguration();
        } else {
            Logger.warn(BaseAppModule.class, "***** Configuring our app to use our beta endpoint *****");
            return new BetaSmartReceiptsHostConfiguration();
        }
    }

    @Provides
    @ApplicationScope
    public static OkHttpClient provideOkHttpClient(Context context,
                                                   MutableIdentityStore mutableIdentityStore) {
        return new SmartReceiptsOkHttpClientFactory(context, mutableIdentityStore).newInstance();
    }

}
