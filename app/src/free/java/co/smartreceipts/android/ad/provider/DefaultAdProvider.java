package co.smartreceipts.android.ad.provider;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.admob.ClassicBannerAdPresenter;
import co.smartreceipts.android.di.scopes.ActivityScope;

@ActivityScope
public class DefaultAdProvider implements Provider<AdPresenter> {

    private static final int RANDOM_MAX = 100;
    private static final int MARKETS_AD_FREQUENCY = 5; // Out of 100

    private final Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider;

    private final Random random = new Random();

    @Inject
    public DefaultAdProvider(@NonNull Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider) {
        this.classicBannerAdPresenterProvider = Preconditions.checkNotNull(classicBannerAdPresenterProvider);
    }

    @Override
    public AdPresenter get() {
        return classicBannerAdPresenterProvider.get();
    }

    private boolean shouldShowMarketsAd() {
        return MARKETS_AD_FREQUENCY >= random.nextInt(RANDOM_MAX + 1);
    }

}
