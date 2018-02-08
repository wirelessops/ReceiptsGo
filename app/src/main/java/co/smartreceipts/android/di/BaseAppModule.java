package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module(includes = {TooltipStorageModule.class,
                    NetworkingModule.class,
                    LocalRepositoryModule.class,
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

}
