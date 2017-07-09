package co.smartreceipts.android.di;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.provider.DefaultAdProvider;
import co.smartreceipts.android.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityAdModule {

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(DefaultAdProvider defaultAdProvider) {
        return defaultAdProvider.get();
    }

}
