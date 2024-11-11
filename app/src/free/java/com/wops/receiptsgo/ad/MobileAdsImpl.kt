package com.wops.receiptsgo.ad

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.RequestConfiguration
import javax.inject.Inject
import com.google.android.gms.ads.MobileAds as GoogleMobileAds


class MobileAdsImpl @Inject constructor(private val context: Context) : MobileAds {

    override fun initialize() {
        GoogleMobileAds.initialize(context)

        GoogleMobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(
                    listOf(
                        AdRequest.DEVICE_ID_EMULATOR,
                        "152286E4E1351DECFB54AAD85E9FB5E1",
                        "2F3D337014FFE9562D8F99717172A9BE"
                    )
                )
                .build()
        )
    }
}