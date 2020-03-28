package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.core.sync.model.SyncState;
import wb.android.storage.StorageManager;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.TripsTable}
 */
public final class TripDatabaseAdapter implements DatabaseAdapter<Trip> {

    private final StorageManager storageManager;
    private final UserPreferenceManager preferences;
    private final SyncStateAdapter syncStateAdapter;

    public TripDatabaseAdapter(@NonNull StorageManager storageManager, @NonNull UserPreferenceManager preferences, @NonNull SyncStateAdapter syncStateAdapter) {
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.syncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @Override
    @NonNull
    public Trip read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(CategoriesTable.COLUMN_ID);
        final int uuidIndex = cursor.getColumnIndex(CategoriesTable.COLUMN_UUID);
        final int nameIndex = cursor.getColumnIndex(TripsTable.COLUMN_NAME);
        final int fromIndex = cursor.getColumnIndex(TripsTable.COLUMN_FROM);
        final int toIndex = cursor.getColumnIndex(TripsTable.COLUMN_TO);
        final int fromTimeZoneIndex = cursor.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
        final int toTimeZoneIndex = cursor.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
        final int commentIndex = cursor.getColumnIndex(TripsTable.COLUMN_COMMENT);
        final int costCenterIndex = cursor.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
        final int defaultCurrencyIndex = cursor.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);
        final int nameHiddenFromAutoCompleteIndex = cursor.getColumnIndex(TripsTable.COLUMN_NAME_HIDDEN_AUTO_COMPLETE);
        final int commentHiddenFromAutoCompleteIndex = cursor.getColumnIndex(TripsTable.COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE);
        final int costCenterHiddenFromAutoCompleteIndex = cursor.getColumnIndex(TripsTable.COLUMN_COSTCENTER_HIDDEN_AUTO_COMPLETE);

        final int id = cursor.getInt(idIndex);
        final UUID uuid = UUID.fromString(cursor.getString(uuidIndex));
        final String name = cursor.getString(nameIndex);
        final long from = cursor.getLong(fromIndex);
        final long to = cursor.getLong(toIndex);
        final String fromTimeZone = cursor.getString(fromTimeZoneIndex);
        final String toTimeZone = cursor.getString(toTimeZoneIndex);
        final String comment = cursor.getString(commentIndex);
        final String costCenter = cursor.getString(costCenterIndex);
        final String defaultCurrency = cursor.getString(defaultCurrencyIndex);
        final boolean isNameHiddenFromAutoComplete = cursor.getInt(nameHiddenFromAutoCompleteIndex) > 0;
        final boolean isCommentHiddenFromAutoComplete = cursor.getInt(commentHiddenFromAutoCompleteIndex) > 0;
        final boolean isCostCenterHiddenFromAutoComplete = cursor.getInt(costCenterHiddenFromAutoCompleteIndex) > 0;
        final SyncState syncState = syncStateAdapter.read(cursor);

        return new TripBuilderFactory()
                .setId(id)
                .setUuid(uuid)
                .setDirectory(storageManager.mkdir(name))
                .setStartDate(from)
                .setEndDate(to)
                .setStartTimeZone(fromTimeZone)
                .setEndTimeZone(toTimeZone)
                .setComment(comment)
                .setCostCenter(costCenter)
                .setDefaultCurrency(defaultCurrency, preferences.get(UserPreference.General.DefaultCurrency))
                .setSyncState(syncState)
                .build();
    }

    @Override
    @NonNull
    public ContentValues write(@NonNull Trip trip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();
        values.put(TripsTable.COLUMN_NAME, trip.getName());
        values.put(TripsTable.COLUMN_FROM, trip.getStartDate().getTime());
        values.put(TripsTable.COLUMN_TO, trip.getEndDate().getTime());
        values.put(TripsTable.COLUMN_FROM_TIMEZONE, trip.getStartTimeZone().getID());
        values.put(TripsTable.COLUMN_TO_TIMEZONE, trip.getEndTimeZone().getID());
        values.put(TripsTable.COLUMN_COMMENT, trip.getComment());
        values.put(TripsTable.COLUMN_COST_CENTER, trip.getCostCenter());
        values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, trip.getDefaultCurrencyCode());
        values.put(TripsTable.COLUMN_UUID, trip.getUuid().toString());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(syncStateAdapter.write(trip.getSyncState()));
        } else {
            values.putAll(syncStateAdapter.writeUnsynced(trip.getSyncState()));
        }
        return values;
    }

    @Override
    @NonNull
    public Trip build(@NonNull Trip trip, int primaryKey, @NonNull UUID uuid, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new TripBuilderFactory(trip)
                .setId(primaryKey)
                .setUuid(uuid)
                .setDirectory(storageManager.getFile(trip.getName()))
                .setSyncState(syncStateAdapter.get(trip.getSyncState(), databaseOperationMetadata))
                .build();
    }
}
