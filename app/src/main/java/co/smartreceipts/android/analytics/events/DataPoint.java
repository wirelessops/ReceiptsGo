package co.smartreceipts.android.analytics.events;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class DataPoint {

    private final String mName;
    private final Object mValue;

    public DataPoint(@NonNull String name, @NonNull Object value) {
        mName = Preconditions.checkNotNull(name);
        mValue = Preconditions.checkNotNull(value);
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public String getValue() {
        final String value = mValue.toString();
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "\'" + getName() + "\':\'" + getValue() + "\'";
    }
}
