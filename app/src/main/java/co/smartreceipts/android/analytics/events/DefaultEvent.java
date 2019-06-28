package co.smartreceipts.android.analytics.events;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DefaultEvent implements Event {

    private final Category mCategory;
    private final Name mName;
    protected final List<DataPoint> mDataPoints;

    public DefaultEvent(@NonNull Category category, @NonNull String name) {
        this(category, new ImmutableName(name));
    }

    public DefaultEvent(@NonNull Category category, @NonNull Name name) {
        this(category, name, Collections.<DataPoint>emptyList());
    }

    public DefaultEvent(@NonNull Category category, @NonNull Name name, @NonNull List<DataPoint> dataPoints) {
        mCategory = Preconditions.checkNotNull(category);
        mName = Preconditions.checkNotNull(name);
        mDataPoints = new ArrayList<>(Preconditions.checkNotNull(dataPoints));
    }

    @NonNull
    @Override
    public Category category() {
        return mCategory;
    }

    @NonNull
    @Override
    public Name name() {
        return mName;
    }

    @NonNull
    @Override
    public List<DataPoint> getDataPoints() {
        return mDataPoints;
    }

    @Override
    public String toString() {
        return "<" + category() + "::" + name() + ">";
    }
}
