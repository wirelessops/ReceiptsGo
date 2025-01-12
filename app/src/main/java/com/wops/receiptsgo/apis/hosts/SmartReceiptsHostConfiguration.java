package com.wops.receiptsgo.apis.hosts;

import androidx.annotation.NonNull;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    @NonNull
    @Override
    public String getBaseUrl() {
        return "http://example.com";
    }

}
