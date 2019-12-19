package co.smartreceipts.android.di;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.EmptyBannerAdPresenter;
import co.smartreceipts.core.di.scopes.ActivityScope;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class SmartReceiptsActivityAdModule {

    @Binds
    @ActivityScope
    public abstract AdPresenter provideAdPresenter(EmptyBannerAdPresenter emptyBannerAdPresenter);

}
