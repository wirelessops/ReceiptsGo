package co.smartreceipts.android.ad

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import javax.inject.Inject


class MobileAdsInitializer @Inject constructor(private val context: Context) {

    fun initialize() {
        MobileAds.initialize(context)

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(
                    listOf(
                        AdRequest.DEVICE_ID_EMULATOR,
                        "EF80F770CD5E7AA481D559EC1037AE2D",
                        "BFB48A3556EED9C87CB3AD907780D610",
                        "E03AEBCB2894909B8E4EC87C0368C242",
                        "B48FF89819FAB2B50FE3E5240FCD9741",
                        "F868E3E348ACF850C6454323A90E2F09",
                        "E5709A4C156B990EFD896EC4719AA8F0"
                    )
                )
                .build()
        )
    }
}