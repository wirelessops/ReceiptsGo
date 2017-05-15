package co.smartreceipts.android.di;

import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.impl.ClassicBannerAdPresenter;
import co.smartreceipts.android.ad.impl.NativeBannerAdPresenter;
import co.smartreceipts.android.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityAdModule {

    private final static boolean USE_CLASSIC_ADS = true;

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider,
                                                 Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider) {
        return USE_CLASSIC_ADS ? classicBannerAdPresenterProvider.get() : nativeBannerAdPresenterProvider.get();
    }

}
