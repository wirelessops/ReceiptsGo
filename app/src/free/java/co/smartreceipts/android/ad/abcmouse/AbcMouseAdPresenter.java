package co.smartreceipts.android.ad.abcmouse;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.ad.BaseAdPresenter;
import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.ad.admob.widget.ClassicAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

@ActivityScope
public class AbcMouseAdPresenter extends BaseAdPresenter {

    @Inject
    AbcMouseAdPresenter(PurchaseWallet purchaseWallet, Analytics analytics, PurchaseManager purchaseManager) {
        super(purchaseWallet, analytics, purchaseManager);
    }

    @Override
    public BannerAdView initAdView(@NonNull Activity activity) {
        return new AbcMouseAdView().init(activity);
    }
}
