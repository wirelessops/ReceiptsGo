package co.smartreceipts.android.ad;

import com.google.android.gms.ads.AdRequest;

public class AdRequestHelper {

    public static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BFB48A3556EED9C87CB3AD907780D610")
                .addTestDevice("E03AEBCB2894909B8E4EC87C0368C242")
                .addTestDevice("B48FF89819FAB2B50FE3E5240FCD9741")
                .addTestDevice("F868E3E348ACF850C6454323A90E2F09") // Julia Soboleva
                .build();
    }
}
