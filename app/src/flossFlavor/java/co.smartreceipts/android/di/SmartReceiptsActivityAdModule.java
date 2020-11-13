package co.smartreceipts.android.di;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.EmptyBannerAdPresenter;
import co.smartreceipts.android.ad.EmptyInterstitialAdPresenter;
import co.smartreceipts.android.ad.InterstitialAdPresenter;
import co.smartreceipts.core.di.scopes.ActivityScope;
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
