package co.smartreceipts.android.di;

import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.impl.ClassicBannerAdPresenter;
import co.smartreceipts.android.ad.impl.NativeBannerAdPresenter;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.utils.FeatureFlags;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityAdModule {

    @Provides
    @ActivityScope
    public static AdPresenter provideAdPresenter(Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider,
                                                 Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider) {
        return FeatureFlags.UseNativeAds.isEnabled() ? nativeBannerAdPresenterProvider.get() : classicBannerAdPresenterProvider.get();
    }

}
