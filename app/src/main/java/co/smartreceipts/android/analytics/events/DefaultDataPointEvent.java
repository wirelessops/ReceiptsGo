package co.smartreceipts.android.analytics.events;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class DefaultDataPointEvent extends DefaultEvent implements DataPointEvent {

    public DefaultDataPointEvent(@NonNull Event event) {
        super(event.category(), event.name());
    }

    @Override
    public DataPointEvent addDataPoint(@NonNull DataPoint dataPoint) {
        mDataPoints.add(Preconditions.checkNotNull(dataPoint));
        return this;
    }
}
