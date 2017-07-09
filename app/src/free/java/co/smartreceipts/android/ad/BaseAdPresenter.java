package co.smartreceipts.android.ad;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.AdListener;

import java.lang.ref.WeakReference;
import java.util.Random;

import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;

public abstract class BaseAdPresenter implements AdPresenter {

    private static final int RANDOM_MAX = 100;
    private static final int UPSELL_FREQUENCY = 1; // Out of 100

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.SubclassAds.toString();
    private static final String SHOW_AD = "pref1";

    private final PurchaseWallet purchaseWallet;
    private final Analytics analytics;
    private final PurchaseManager purchaseManager;

    private WeakReference<BannerAdView> adViewReference;

    public BaseAdPresenter(PurchaseWallet purchaseWallet, Analytics analytics, PurchaseManager purchaseManager) {
        this.purchaseWallet = purchaseWallet;
        this.analytics = analytics;
        this.purchaseManager = purchaseManager;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity) {
        BannerAdView adView = initAdView(activity);

        adViewReference = new WeakReference<>(adView);

        if (adView != null) {
            if (shouldShowAds(activity)) {
                if (shouldShowUpsell()) {
                    analytics.record(Events.Purchases.AdUpsellShown);
                    adView.showUpsell();
                } else {
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // If we fail to load the ad, just hide it
                            analytics.record(Events.Purchases.AdUpsellShownOnFailure);
                            adView.showUpsell();
                        }

                        @Override
                        public void onAdLoaded() {
                            adView.showAd();
                        }
                    });
                    adView.loadAdDelayed();
                }

                adView.setUpsellClickListener(view -> {
                    analytics.record(Events.Purchases.AdUpsellTapped);
                    this.purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.AdBanner);
                });

            } else {
                adView.hide();
            }
        }
    }

    @Override
    public void onResume() {
        final BannerAdView adView = adViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView.getContext())) {
                adView.resume();
            } else {
                adView.hide();
            }
        }
    }

    @Override
    public void onPause() {
        final BannerAdView adView = adViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView.getContext())) {
                adView.pause();
            } else {
                adView.hide();
            }
        }
    }

    @Override
    public void onDestroy() {
        final BannerAdView adView = adViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView.getContext())) {
                adView.destroy();
            } else {
                adView.hide();
            }
        }
    }

    public abstract BannerAdView initAdView(@NonNull Activity activity);

    private boolean shouldShowAds(@NonNull Context activityContext) {
        final boolean hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final boolean areAdsEnabledLocally = activityContext.getSharedPreferences(AD_PREFERENECES, 0).getBoolean(SHOW_AD, true);
        return areAdsEnabledLocally && !hasProSubscription;
    }

    private boolean shouldShowUpsell() {
        final Random random = new Random(SystemClock.uptimeMillis());
        return UPSELL_FREQUENCY >= random.nextInt(RANDOM_MAX + 1);
    }

    @Override
    public void onSuccessPlusPurchase() {
        final BannerAdView adView = adViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView.getContext())) {
                Logger.warn(this, "Showing the original ad following a purchase");
                adView.showAd();
            } else {
                Logger.info(this, "Hiding the original ad following a purchase");
                adView.hide();
            }
        }
    }
}
