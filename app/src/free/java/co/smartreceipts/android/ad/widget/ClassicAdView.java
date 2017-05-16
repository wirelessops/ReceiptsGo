package co.smartreceipts.android.ad.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;

import static co.smartreceipts.android.ad.AdRequestHelper.getAdRequest;

public class ClassicAdView implements BannerAdView {

    private AdView adView;
    private Button upsellButton;

    @Override
    public BannerAdView init(Activity activity) {
        final ViewGroup container = (ViewGroup) activity.findViewById(R.id.adView_container);
        upsellButton = (Button) activity.findViewById(R.id.adView_upsell);

        adView = new AdView(activity);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(activity.getResources().getString(R.string.classicAdUnitId));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
}
