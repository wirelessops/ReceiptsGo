package co.smartreceipts.android.persistence.database.controllers.results;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Trip;

public class ForeignKeyGetResult<ModelType> {

    private final Trip trip;
    private final List<ModelType> model;
    private final Throwable throwable;

    public ForeignKeyGetResult(@NonNull Trip trip, @NonNull List<ModelType> model) {
        this(trip, model, null);
    }

    public ForeignKeyGetResult(@NonNull Trip trip, @NonNull Throwable throwable) {
        this(trip, null, throwable);
    }

    public ForeignKeyGetResult(@NonNull Trip trip, @Nullable List<ModelType> model, @Nullable Throwable throwable) {
        this.trip = trip;
        this.model = model;
        this.throwable = throwable;
    }

    @NonNull
    public Trip getTrip() {
        return trip;
    }

    @Nullable
    public List<ModelType> get() {
        return model;
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

}
