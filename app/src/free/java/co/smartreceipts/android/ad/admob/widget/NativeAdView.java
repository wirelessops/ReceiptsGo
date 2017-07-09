package co.smartreceipts.android.ad.admob.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import butterknife.ButterKnife;
import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static co.smartreceipts.android.ad.admob.AdRequestHelper.getAdRequest;

public class NativeAdView implements BannerAdView {

    private NativeExpressAdView adView;
    private Button upsellButton;

    @Override
    public BannerAdView init(Activity activity) {
        final ViewGroup container = ButterKnife.findById(activity, R.id.adView_container);
        upsellButton = ButterKnife.findById(activity, R.id.adView_upsell);

        adView = new NativeExpressAdView(activity);
        adView.setAdSize(calculateAdSize());
        adView.setAdUnitId(activity.getResources().getString(R.string.adUnitId));

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        adView.setLayoutParams(params);

        container.addView(adView);

        return this;
    }

    @Override
    public void resume() {
        adView.resume();
    }

    @Override
    public void pause() {
        adView.pause();
    }

    @Override
    public void destroy() {
        adView.destroy();
    }

    @Override
    public void loadAdDelayed() {
        adView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    adView.loadAd(getAdRequest());
                } catch (Exception e) {
                    Logger.error(this, "Swallowing ad load exception... ", e);
                    // Swallowing all exception b/c I'm lazy and don't want to handle activity finishing states
                }
            }
        }, LOADING_DELAY);
    }

    @Override
    public void showUpsell() {
        adView.setVisibility(View.GONE);
        upsellButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showAd() {
        adView.setVisibility(View.VISIBLE);
        upsellButton.setVisibility(View.GONE);
    }

    @Override
    public void hide() {
        adView.setVisibility(View.GONE);
        upsellButton.setVisibility(View.GONE);
    }

    @Override
    public void setAdListener(AdListener listener) {
        adView.setAdListener(listener);
    }

    @Override
    public void setUpsellClickListener(View.OnClickListener listener) {
        upsellButton.setOnClickListener(listener);
    }

    @Override
    public Context getContext() {
        return adView.getContext();
    }

    @NonNull
    private AdSize calculateAdSize() {
        float density = Resources.getSystem().getDisplayMetrics().density;
        int heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
        int heightDps = (int) (heightPixels / density);

        int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        int widthDps = (int) (widthPixels / density);

        // Use FULL_WIDTH unless the screen width is greater than the max width
        int adWidth = (widthDps < 1200) ? AdSize.FULL_WIDTH : 1200;

        if (heightDps < 700) {
            return new AdSize(adWidth, 80);
        } else if (heightDps < 1000) {
            return new AdSize(adWidth, 100);
        } else {
            return new AdSize(adWidth, 130);
        }
    }
}
