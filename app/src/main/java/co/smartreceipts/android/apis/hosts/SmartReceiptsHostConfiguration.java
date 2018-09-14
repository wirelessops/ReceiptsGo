package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.identity.store.IdentityStore;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://www.smartreceipts.co";
    }

}
