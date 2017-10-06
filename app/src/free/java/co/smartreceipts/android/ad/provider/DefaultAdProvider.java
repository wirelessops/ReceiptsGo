package co.smartreceipts.android.ad.provider;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.admob.presenter.ClassicBannerAdPresenter;
import co.smartreceipts.android.ad.markets.MarketsComAdPresenter;
import co.smartreceipts.android.ad.region.RegionChecker;
import co.smartreceipts.android.di.scopes.ActivityScope;

@ActivityScope
public class DefaultAdProvider implements Provider<AdPresenter> {

    private static final int RANDOM_MAX = 100;
    private static final int MARKETS_AD_FREQUENCY = 5; // Out of 100

    private final RegionChecker regionChecker;
    private final Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider;
    private final Provider<MarketsComAdPresenter> marketsComAdPresenterProvider;

    private final Random random = new Random();

    @Inject
    public DefaultAdProvider(@NonNull RegionChecker regionChecker,
                             @NonNull Provider<ClassicBannerAdPresenter> classicBannerAdPresenterProvider,
                             @NonNull Provider<MarketsComAdPresenter> marketsComAdPresenterProvider) {
        this.regionChecker = Preconditions.checkNotNull(regionChecker);
        this.classicBannerAdPresenterProvider = Preconditions.checkNotNull(classicBannerAdPresenterProvider);
        this.marketsComAdPresenterProvider = Preconditions.checkNotNull(marketsComAdPresenterProvider);
    }

    @Override
    public AdPresenter get() {
        if (regionChecker.isInWesternEurope() && shouldShowMarketsAd()) {
            return marketsComAdPresenterProvider.get();
        } else {
            return classicBannerAdPresenterProvider.get();
        }
    }

    private boolean shouldShowMarketsAd() {
        return MARKETS_AD_FREQUENCY >= random.nextInt(RANDOM_MAX + 1);
    }

}
