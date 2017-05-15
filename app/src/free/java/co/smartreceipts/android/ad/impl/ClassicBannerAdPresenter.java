package co.smartreceipts.android.ad.impl;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.ad.BaseAdPresenter;
import co.smartreceipts.android.ad.widget.BannerAdView;
import co.smartreceipts.android.ad.widget.ClassicAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

@ActivityScope
public class ClassicBannerAdPresenter extends BaseAdPresenter {

    @Inject
    ClassicBannerAdPresenter(PurchaseWallet purchaseWallet, Analytics analytics, PurchaseManager purchaseManager) {
        super(purchaseWallet, analytics, purchaseManager);
    }

    @Override
    public BannerAdView initAdView(@NonNull Activity activity) {
        return new ClassicAdView().init(activity);
    }
}
