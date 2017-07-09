package co.smartreceipts.android.ad.provider;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;
import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.admob.presenter.ClassicBannerAdPresenter;
import co.smartreceipts.android.ad.admob.presenter.NativeBannerAdPresenter;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.utils.FeatureFlags;

@ActivityScope
public class DefaultAdProvider implements Provider<AdPresenter> {

    private final Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider;
    private final Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider;

    @Inject
    public DefaultAdProvider(@NonNull Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider,
                             @NonNull Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider) {
        this.classicBannerAdPresenterProvider = Preconditions.checkNotNull(classicBannerAdPresenterProvider);
        this.nativeBannerAdPresenterProvider = Preconditions.checkNotNull(nativeBannerAdPresenterProvider);
    }

    @Override
    public AdPresenter get() {
        return FeatureFlags.UseNativeAds.isEnabled() ? nativeBannerAdPresenterProvider.get() : classicBannerAdPresenterProvider.get();
    }
}
