package co.smartreceipts.android.analytics.events;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class ImmutableName implements Event.Name {

    private final String mName;

    public ImmutableName(@NonNull String name) {
        mName = Preconditions.checkNotNull(name);
    }

    @NonNull
    @Override
    public String name() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
