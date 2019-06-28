package co.smartreceipts.android.persistence.database.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Trip;

public interface TripForeignKeyTableEventsListener<T> extends TableEventsListener<T> {

    void onGetSuccess(@NonNull List<T> list, @NonNull Trip trip);

    void onGetFailure(@Nullable Throwable e, @NonNull Trip trip);
}
