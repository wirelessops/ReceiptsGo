package com.wops.analytics;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.Event;

class FirebaseAnalytics implements Analytics {


    private final com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics;

    @Inject
    public FirebaseAnalytics(Context context) {
        firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context.getApplicationContext());
        String testLabSetting = Settings.System.getString(context.getContentResolver(), "firebase.test.lab");
        if ("true".equals(testLabSetting)) {
            firebaseAnalytics.getInstance(context.getApplicationContext()).setAnalyticsCollectionEnabled(false);
        }
    }

    @Override
    public void record(@NonNull Event event) {
        Bundle b = new Bundle();
        List<DataPoint> dataPoints = event.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            b.putString(dataPoint.getName(), dataPoint.getValue());
        }
        firebaseAnalytics.logEvent(event.name().name(), b);
    }
}
