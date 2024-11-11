package com.wops.receiptsgo.di;

import android.app.Application;
import android.content.Context;

import com.wops.receiptsgo.SmartReceiptsApplication;
import com.wops.receiptsgo.autocomplete.di.AutoCompleteModule;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module(includes = {TooltipStorageModule.class,
        NetworkingModule.class,
        LocalRepositoryModule.class,
        SharedPreferencesModule.class,
        ImageLoadingModule.class,
        AutoCompleteModule.class,
        ExecutorsModule.class,
        RxModule.class,
        IdentityModule.class,
        ConfigurationModule.class})
public class BaseAppModule {

    private final SmartReceiptsApplication application;

    public BaseAppModule(SmartReceiptsApplication application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    Context provideContext() {
        return application;
    }

    @Provides
    @ApplicationScope
    Application provideApplication() {
        return application;
    }

}
