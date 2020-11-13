package co.smartreceipts.android.di;

import co.smartreceipts.android.ad.AdMobInterstitialAdPresenter;
import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.BannerAdPresenter;
import co.smartreceipts.android.ad.InterstitialAdPresenter;
import co.smartreceipts.core.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityAdModule {

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(BannerAdPresenter bannerAdPresenter) {
        return bannerAdPresenter;
    }

    @Provides
    @ActivityScope
    public static InterstitialAdPresenter provideInterstitialAdPresenter(AdMobInterstitialAdPresenter presenter) {
        return presenter;
    }

}
