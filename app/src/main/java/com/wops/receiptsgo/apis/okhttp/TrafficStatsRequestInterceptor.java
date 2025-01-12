package com.wops.receiptsgo.apis.okhttp;


import android.net.TrafficStats;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * To avoid StrictMode violations that occur in Android O, we tag all of our OkHttp requests with
 * a predetermined traffic stats tag as detailed here:
 *
 * https://github.com/square/okhttp/issues/3537
 *
 * Please note that we don't use the SocketFactory variant as this was failing with the default. The
 * interceptor approach seems light weight enough and works for our case
 */
public class TrafficStatsRequestInterceptor implements Interceptor{

    private static final int TRAFFIC_STATS_TAG = 1;

    @Override
    public Response intercept(Chain chain) throws IOException {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        return chain.proceed(chain.request());
    }
}
