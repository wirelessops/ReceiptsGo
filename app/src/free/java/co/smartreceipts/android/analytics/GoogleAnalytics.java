package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;
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