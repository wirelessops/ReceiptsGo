package com.wops.receiptsgo.di;

import android.app.Application;
import android.content.Context;

import com.wops.receiptsgo.ReceiptsGoApplication;
import com.wops.receiptsgo.autocomplete.di.AutoCompleteModule;
import com.wops.core.di.scopes.ApplicationScope;
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

    private final ReceiptsGoApplication application;

    public BaseAppModule(ReceiptsGoApplication application) {
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
