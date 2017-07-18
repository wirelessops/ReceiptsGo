package co.smartreceipts.android.ad.markets;

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

import java.util.Random;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ad.admob.widget.BannerAdView;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;

public class MarketsComAdView implements BannerAdView {

    private View adView;
    private Button upsellButton;

    @Override
    public BannerAdView init(@NonNull Activity activity, @NonNull Analytics analytics) {
        final ViewGroup container = (ViewGroup) activity.findViewById(R.id.adView_container);
        final LayoutInflater inflater = LayoutInflater.from(activity);

        upsellButton = (Button) activity.findViewById(R.id.adView_upsell);

        adView = inflater.inflate(R.layout.custom_ad_placement, container, false);

        final ImageView imageView = (ImageView) adView.findViewById(R.id.custom_ad_image);
        final int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        final float adHeightPixels = activity.getResources().getDimension(R.dimen.custom_ad_height);
        final float ratio = widthPixels / adHeightPixels;

        final boolean showBtc = new Random().nextBoolean();
        if (ratio > 9.35f) {
            // If we're at a ratio greater than 842/90 = 11.25, scale up to the larger image (note: 842 = (956+728)/ 2)
            if (showBtc) {
                imageView.setImageResource(R.drawable.markets_btc_956x90);
            } else {
                imageView.setImageResource(R.drawable.markets_eth_956x90);
            }
        } else {
            if (showBtc) {
                imageView.setImageResource(R.drawable.markets_btc_728x90);
            } else {
                imageView.setImageResource(R.drawable.markets_eth_728x90);
            }
        }

        container.addView(adView);

        adView.setOnClickListener(v -> {
            analytics.record(new DefaultDataPointEvent(Events.Ads.MarketsAdClicked).addDataPoint(new DataPoint("btc", showBtc)));
            if (showBtc) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://serv.markets.com/promoRedirect?key=ej0xNTcyMjcwOCZsPTE1Njk0MDEyJnA9MzQ2MzA%3D")));
            } else {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://serv.markets.com/promoRedirect?key=ej0xNTc2NTc0NSZsPTE1NzY3NjUwJnA9MzQ2MzA%3D")));
            }
        });

        analytics.record(new DefaultDataPointEvent(Events.Ads.MarketsAdShown).addDataPoint(new DataPoint("btc", showBtc)));

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
