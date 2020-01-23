package co.smartreceipts.analytics.impl;

import androidx.annotation.NonNull;

import java.util.List;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.DataPoint;
import co.smartreceipts.analytics.events.Event;
import co.smartreceipts.analytics.log.Logger;

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
