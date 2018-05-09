package co.smartreceipts.android.ad.admob;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

public class AdRequestHelper {

    public static AdRequest getAdRequest(@NonNull UserPreferenceManager userPreferenceManager) {
        final AdRequest.Builder builder = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BFB48A3556EED9C87CB3AD907780D610")
                .addTestDevice("E03AEBCB2894909B8E4EC87C0368C242")
                .addTestDevice("B48FF89819FAB2B50FE3E5240FCD9741")
                .addTestDevice("F868E3E348ACF850C6454323A90E2F09");
        if (userPreferenceManager.get(UserPreference.Privacy.EnableAdPersonalization)) {
            final Bundle extras = new Bundle();
            extras.putString("npa", "1");
            builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }
        return builder.build();
    }
}
