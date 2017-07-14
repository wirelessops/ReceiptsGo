package co.smartreceipts.android.ad.abcmouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.utils.log.Logger;

import static co.smartreceipts.android.ad.admob.AdRequestHelper.getAdRequest;

public class AbcMouseAdView implements BannerAdView {

    private View adView;
    private Button upsellButton;

    @Override
    public BannerAdView init(@NonNull Activity activity, @NonNull Analytics analytics) {
        final ViewGroup container = (ViewGroup) activity.findViewById(R.id.adView_container);
        final LayoutInflater inflater = LayoutInflater.from(activity);

        upsellButton = (Button) activity.findViewById(R.id.adView_upsell);

        adView = inflater.inflate(R.layout.abc_mouse_ad, container, false);

        final ImageView imageView = (ImageView) adView.findViewById(R.id.abc_mouse_image);
        final int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        final float adHeightPixels = activity.getResources().getDimension(R.dimen.abc_mouse_ad_height);
        final float ratio = widthPixels / adHeightPixels;

        if (ratio > 11.25f) {
            // If we're at a ratio greater than 900/80 = 11.25, scale up to the 1200x80 image (note: 900 = (1200+600)/ 2)
            imageView.setImageResource(R.drawable.abc_mouse_1200x80);
        } else {
            // Else, use the 600x80 one
            imageView.setImageResource(R.drawable.abc_mouse_600x80);
        }

        container.addView(adView);

        adView.setOnClickListener(v -> {
            analytics.record(Events.Ads.AbcAdClicked);
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kqzyfj.com/click-8372510-12795763")));
        });

        analytics.record(Events.Ads.AbcAdClicked);

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
