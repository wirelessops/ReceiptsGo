package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.TripsTable;

/**
 * Defines the primary key for the {@link TripsTable}
 */
public final class TripPrimaryKey implements PrimaryKey<Trip, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return TripsTable.COLUMN_ID;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull Trip trip) {
        return trip.getId();
    }
}
