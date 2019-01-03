package co.smartreceipts.android.di;

import android.app.Application;
import android.content.Context;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.autocomplete.di.AutoCompleteModule;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module(includes = {TooltipStorageModule.class,
                    NetworkingModule.class,
                    LocalRepositoryModule.class,
                    SharedPreferencesModule.class,
                    ImageLoadingModule.class,
                    ConfigurationModule.class,
                    AutoCompleteModule.class,
                    ExecutorsModule.class,
                    RxModule.class })
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
