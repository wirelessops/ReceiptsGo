package co.smartreceipts.android.ad.abcmouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.utils.log.Logger;

import static co.smartreceipts.android.ad.admob.AdRequestHelper.getAdRequest;

public class AbcMouseAdView implements BannerAdView {

    private View adView;
    private Button upsellButton;

    @Override
    public BannerAdView init(@NonNull Activity activity) {
        final ViewGroup container = (ViewGroup) activity.findViewById(R.id.adView_container);
        final LayoutInflater inflater = LayoutInflater.from(activity);

        upsellButton = (Button) activity.findViewById(R.id.adView_upsell);

        adView = inflater.inflate(R.layout.abc_mouse_ad, container, false);
        container.addView(adView);

        adView.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kqzyfj.com/click-8372510-12795763")));
        });

        return this;
    }

    @Override
    public void resume() {
        // No-op
    }

    @Override
    public void pause() {
        // No-op
    }

    @Override
    public void destroy() {
        // No-op
    }

    @Override
    public void loadAdDelayed() {

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
