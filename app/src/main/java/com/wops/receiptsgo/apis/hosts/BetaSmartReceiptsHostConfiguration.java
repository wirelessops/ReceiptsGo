package com.wops.receiptsgo.apis.hosts;

import androidx.annotation.NonNull;

public class BetaSmartReceiptsHostConfiguration implements HostConfiguration {

    @NonNull
    @Override
    public String getBaseUrl() {
        return "http://www.example.com";
    }

}