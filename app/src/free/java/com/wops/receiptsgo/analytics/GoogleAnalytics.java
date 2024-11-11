package com.wops.receiptsgo.analytics;

import androidx.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.Event;
import com.wops.analytics.log.Logger;
import com.wops.core.di.scopes.ApplicationScope;
import dagger.Lazy;

@ApplicationScope
public class GoogleAnalytics implements Analytics {

    private final Lazy<Tracker> tracker;

    @Inject
    public GoogleAnalytics(@NonNull Lazy<Tracker> tracker) {
        this.tracker = Preconditions.checkNotNull(tracker);
    }

    @Override
    public synchronized void record(@NonNull Event event) {
        try {
            tracker.get().send(new HitBuilders.EventBuilder(event.category().name(), event.name().name()).setLabel(getLabelString(event.getDataPoints())).build());
        } catch (Exception e) {
            Logger.error(this, "Swallowing GA Exception", e);
        }
    }

    @NonNull
    private String getLabelString(@NonNull List<DataPoint> dataPoints) {
        if (!dataPoints.isEmpty()) {
            final StringBuilder stringBuilder = new StringBuilder("{");
            final String separatorChar = ",";
            String currentSeparator = "";
            for (int i = 0; i < dataPoints.size(); i++) {
                stringBuilder.append(currentSeparator).append(dataPoints.get(i).toString());
                currentSeparator = separatorChar;
            }
            return stringBuilder.append("}").toString();
        } else {
            return "";
        }
    }

}