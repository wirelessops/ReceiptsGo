package com.wops.receiptsgo.di;

import com.wops.receiptsgo.ad.AdPresenter;
import com.wops.receiptsgo.ad.EmptyBannerAdPresenter;
import com.wops.receiptsgo.ad.EmptyInterstitialAdPresenter;
import com.wops.receiptsgo.ad.InterstitialAdPresenter;
import com.wops.core.di.scopes.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class SmartReceiptsActivityAdModule {

    @Binds
    @ActivityScope
    public abstract AdPresenter provideAdPresenter(EmptyBannerAdPresenter emptyBannerAdPresenter);

    @Provides
    @ActivityScope
    public static InterstitialAdPresenter provideInterstitialAdPresenter(EmptyInterstitialAdPresenter presenter) {
        return presenter;
    }

}
