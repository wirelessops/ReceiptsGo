package co.smartreceipts.android.workers.reports.pdf.renderer.constraints;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class RenderingConstraints {

    private final Map<Class<? extends Constraint>, Constraint<?>> constraintMap = new HashMap<>();

    public void addConstraint(@NonNull Constraint<?> constraint) {
        Preconditions.checkNotNull(constraint);
        this.constraintMap.put(constraint.getClass(), constraint);
    }

    public boolean hasConstraint(@NonNull Class<? extends Constraint> type) {
        Preconditions.checkNotNull(type);
        return constraintMap.containsKey(type);
    }

    public void setConstraints(@NonNull RenderingConstraints renderingConstraints) {
        constraintMap.clear();
        constraintMap.putAll(renderingConstraints.constraintMap);
    }

    @Nullable
    public <T> T getConstraint(@NonNull Class<? extends Constraint<T>> type) {
        Preconditions.checkNotNull(type);
        final Constraint<?> constraint = constraintMap.get(type);
        if (constraint != null) {
            return (T) constraint.value();
        } else {
            return null;
        }
    }

    @NonNull
    public <T> T getConstraint(@NonNull Class<? extends Constraint<T>> type, @NonNull T defaultValue) {
        Preconditions.checkNotNull(type);
        final Constraint<?> constraint = constraintMap.get(type);
        if (constraint != null) {
            return (T) constraint.value();
        } else {
            return defaultValue;
        }
    }

}
