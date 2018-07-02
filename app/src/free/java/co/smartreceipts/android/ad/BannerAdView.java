package co.smartreceipts.android.ad;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View.OnClickListener;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.settings.UserPreferenceManager;

public interface BannerAdView {

    int LOADING_DELAY = 50;

    BannerAdView init(@NonNull Activity activity, @NonNull Analytics analytics, @NonNull UserPreferenceManager userPreferenceManager);

    void resume();

    void pause();

    void destroy();

    /**
     * The {@link AdView#loadAd(AdRequest)} is really slow and cannot be moved off the main thread (ugh).
     * We use this method to slightly defer the ad loading process until the core UI of the app loads, so
     * users can see data immediately
     */
    void loadAdDelayed();

    void showUpsell();

    void showAd();

    void hide();

    void setAdListener(AdListener listener);

    void setUpsellClickListener(OnClickListener listener);

    Context getContext();

}
