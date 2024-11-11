package com.wops.analytics.impl;

import androidx.annotation.NonNull;

import java.util.List;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.Event;
import com.wops.analytics.log.Logger;

public class AnalyticsLogger implements Analytics {

    @Override
    public void record(@NonNull Event event) {
        Logger.info(this, "Logging Event: {} with dataPoints: {}", event, getDataPointsString(event.getDataPoints()));
    }

    @NonNull
    private String getDataPointsString(@NonNull List<DataPoint> dataPoints) {
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
            return "{}";
        }
    }
}
