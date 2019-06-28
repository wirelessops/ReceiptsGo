package co.smartreceipts.android.apis.hosts;

import androidx.annotation.NonNull;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://www.smartreceipts.co";
    }

}
