package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.SmartReceiptsActivityBindingModule;
import co.smartreceipts.android.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent(modules = {
        SmartReceiptsActivitySubcomponent.SmartReceiptsActivityModule.class,
        SmartReceiptsActivityBindingModule.class
})
public interface SmartReceiptsActivitySubcomponent extends AndroidInjector<SmartReceiptsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SmartReceiptsActivity> {
    }

    @Module
    class SmartReceiptsActivityModule {

        @ActivityScope
        @Provides
        NavigationHandler provideNavigationHandler(SmartReceiptsActivity activity, FragmentProvider fragmentProvider) {
            return new NavigationHandler<>(activity, fragmentProvider);
        }
    }
}
