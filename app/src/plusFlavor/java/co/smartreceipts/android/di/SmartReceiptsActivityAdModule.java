package co.smartreceipts.android.di;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.EmptyBannerAdPresenter;
import co.smartreceipts.android.ad.EmptyInterstitialAdPresenter;
import co.smartreceipts.android.ad.InterstitialAdPresenter;
import co.smartreceipts.core.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityAdModule {

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(EmptyBannerAdPresenter emptyBannerAdPresenter) {
        return emptyBannerAdPresenter;
    }

    @Provides
    @ActivityScope
    public static InterstitialAdPresenter provideInterstitialAdPresenter(EmptyInterstitialAdPresenter presenter) {
        return presenter;
    }
}
