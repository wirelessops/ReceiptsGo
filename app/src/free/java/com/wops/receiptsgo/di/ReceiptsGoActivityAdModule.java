package com.wops.receiptsgo.di;


import com.wops.receiptsgo.ad.AdPresenter;
import com.wops.receiptsgo.ad.BannerAdPresenter;
import com.wops.receiptsgo.ad.EmptyBannerAdPresenter;
import com.wops.receiptsgo.ad.EmptyInterstitialAdPresenter;
import com.wops.receiptsgo.ad.InterstitialAdPresenter;
import com.wops.core.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class ReceiptsGoActivityAdModule {

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(EmptyBannerAdPresenter bannerAdPresenter) {
        return bannerAdPresenter;
    }

    @Provides
    @ActivityScope
    public static InterstitialAdPresenter provideInterstitialAdPresenter(EmptyInterstitialAdPresenter presenter) {
        return presenter;
    }

}
