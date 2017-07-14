package co.smartreceipts.android.ad.admob.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.ad.BaseAdPresenter;
import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.ad.admob.widget.NativeAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

@ActivityScope
public class NativeBannerAdPresenter extends BaseAdPresenter {

    @Inject
    NativeBannerAdPresenter(PurchaseWallet purchaseWallet, Analytics analytics, PurchaseManager purchaseManager) {
        super(purchaseWallet, analytics, purchaseManager);
    }

    @Override
    public BannerAdView initAdView(@NonNull Activity activity, @NonNull Analytics analytics) {
        return new NativeAdView().init(activity, analytics);
    }
}
