package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.UUID;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link DistanceTable}
 */
public final class DistanceDatabaseAdapter implements SelectionBackedDatabaseAdapter<Distance, PrimaryKey<Distance, Integer>, Trip> {

    private final Table<Trip, Integer> mTripsTable;
    private final SyncStateAdapter mSyncStateAdapter;

    public DistanceDatabaseAdapter(@NonNull Table<Trip, Integer> tripsTable) {
        this(tripsTable, new SyncStateAdapter());
    }

    public DistanceDatabaseAdapter(@NonNull Table<Trip, Integer> tripsTable, @NonNull SyncStateAdapter syncStateAdapter) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mSyncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @NonNull
    @Override
    public Distance read(@NonNull Cursor cursor) {
        final int parentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT_TRIP_ID);
        final Trip trip = mTripsTable.findByPrimaryKey(cursor.getInt(parentIndex)).blockingGet();
        return readForSelection(cursor, trip, true);
    }


    @NonNull
    @Override
    public Distance readForSelection(@NonNull Cursor cursor, @NonNull Trip trip, boolean isDescending) {
        final int idIndex = cursor.getColumnIndex(DistanceTable.COLUMN_ID);
        final int uuidIndex = cursor.getColumnIndex(DistanceTable.COLUMN_UUID);
        final int locationIndex = cursor.getColumnIndex(DistanceTable.COLUMN_LOCATION);
        final int distanceIndex = cursor.getColumnIndex(DistanceTable.COLUMN_DISTANCE);
        final int dateIndex = cursor.getColumnIndex(DistanceTable.COLUMN_DATE);
        final int timezoneIndex = cursor.getColumnIndex(DistanceTable.COLUMN_TIMEZONE);
        final int rateIndex = cursor.getColumnIndex(DistanceTable.COLUMN_RATE);
        final int rateCurrencyIndex = cursor.getColumnIndex(DistanceTable.COLUMN_RATE_CURRENCY);
        final int commentIndex = cursor.getColumnIndex(DistanceTable.COLUMN_COMMENT);

        final int id = cursor.getInt(idIndex);
        final UUID uuid = UUID.fromString(cursor.getString(uuidIndex));
        final String location = cursor.getString(locationIndex);
        final BigDecimal distance = BigDecimal.valueOf(cursor.getDouble(distanceIndex));
        final long date = cursor.getLong(dateIndex);
        final String timezone = cursor.getString(timezoneIndex);
        final BigDecimal rate = BigDecimal.valueOf(cursor.getDouble(rateIndex));
        final String rateCurrency = cursor.getString(rateCurrencyIndex);
        final String comment = cursor.getString(commentIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);

        return new DistanceBuilderFactory(id)
                .setUuid(uuid)
                .setTrip(trip)
                .setLocation(location)
                .setDistance(distance)
                .setDate(date)
                .setTimezone(timezone)
                .setRate(rate)
                .setCurrency(rateCurrency)
                .setComment(comment)
                .setSyncState(syncState)
                .build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Distance distance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();

        values.put(DistanceTable.COLUMN_PARENT_TRIP_ID, distance.getTrip().getId());
        values.put(DistanceTable.COLUMN_LOCATION, distance.getLocation().trim());
        values.put(DistanceTable.COLUMN_DISTANCE, distance.getDistance().doubleValue());
        values.put(DistanceTable.COLUMN_TIMEZONE, distance.getTimeZone().getID());
        values.put(DistanceTable.COLUMN_DATE, distance.getDate().getTime());
        values.put(DistanceTable.COLUMN_RATE, distance.getRate().doubleValue());
        values.put(DistanceTable.COLUMN_RATE_CURRENCY, distance.getPrice().getCurrencyCode());
        values.put(DistanceTable.COLUMN_COMMENT, distance.getComment().trim());
        values.put(DistanceTable.COLUMN_UUID, distance.getUuid().toString());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(distance.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(distance.getSyncState()));
        }

        return values;
    }

    @NonNull
    @Override
    public Distance build(@NonNull Distance distance, @NonNull PrimaryKey<Distance, Integer> primaryKey, @NonNull UUID uuid,
                          @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new DistanceBuilderFactory(primaryKey.getPrimaryKeyValue(distance), distance)
                .setUuid(uuid)
                .setSyncState(mSyncStateAdapter.get(distance.getSyncState(), databaseOperationMetadata))
                .build();
    }
}
