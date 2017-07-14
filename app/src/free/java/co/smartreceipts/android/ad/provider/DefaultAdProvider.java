package co.smartreceipts.android.ad.provider;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.abcmouse.AbcMouseAdPresenter;
import co.smartreceipts.android.ad.admob.presenter.ClassicBannerAdPresenter;
import co.smartreceipts.android.ad.admob.presenter.NativeBannerAdPresenter;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.utils.FeatureFlags;

@ActivityScope
public class DefaultAdProvider implements Provider<AdPresenter> {

    private static final int RANDOM_MAX = 100;
    private static final int ABC_MOUSE_AD_FREQUENCY = 1; // Out of 100

    private final Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider;
    private final Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider;
    private final Provider<AbcMouseAdPresenter> abcMouseAdPresenterProvider;

    private final Random random = new Random();

    @Inject
    public DefaultAdProvider(@NonNull Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider,
                             @NonNull Provider<NativeBannerAdPresenter> nativeBannerAdPresenterProvider,
                             @NonNull Provider<AbcMouseAdPresenter> abcMouseAdPresenterProvider) {
        this.classicBannerAdPresenterProvider = Preconditions.checkNotNull(classicBannerAdPresenterProvider);
        this.nativeBannerAdPresenterProvider = Preconditions.checkNotNull(nativeBannerAdPresenterProvider);
        this.abcMouseAdPresenterProvider = Preconditions.checkNotNull(abcMouseAdPresenterProvider);
    }

    @Override
    public AdPresenter get() {
        if (shouldShowAbcMouseAd()) {
            return abcMouseAdPresenterProvider.get();
        } else {
            return FeatureFlags.UseNativeAds.isEnabled() ? nativeBannerAdPresenterProvider.get() : classicBannerAdPresenterProvider.get();
        }
    }

    private boolean shouldShowAbcMouseAd() {
        return ABC_MOUSE_AD_FREQUENCY >= random.nextInt(RANDOM_MAX + 1);
    }
}
