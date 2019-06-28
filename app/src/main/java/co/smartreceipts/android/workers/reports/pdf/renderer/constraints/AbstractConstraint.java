package co.smartreceipts.android.workers.reports.pdf.renderer.constraints;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

abstract class AbstractConstraint<T> implements Constraint<T> {

    private final T value;
    private final Class<T> type;

    public AbstractConstraint(@NonNull T value, @NonNull Class<T> type) {
        this.value = Preconditions.checkNotNull(value);
        this.type = Preconditions.checkNotNull(type);
    }

    @NonNull
    @Override
    public T value() {
        return value;
    }

    @NonNull
    @Override
    public Class<T> getType() {
        return type;
    }
}
