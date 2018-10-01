package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.adapters.SelectionBackedDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderBy;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderByColumn;
import co.smartreceipts.android.sync.model.Syncable;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;

/**
 * Extends the {@link AbstractColumnTable} class to provide support for an extra method, {@link #get(Trip)}. We may
 * want to generify this class further (to support other classes beside just {@link Trip} objects in the future), but
 * it'll stay hard-typed for now until this requirement arises...
 *
 * @param <ModelType> the model object that CRUD operations here should return
 * @param <PrimaryKeyType> the primary key type (e.g. Integer, String) that will be used
 */
public abstract class TripForeignKeyAbstractSqlTable<ModelType extends Keyed, PrimaryKeyType> extends AbstractSqlTable<ModelType, PrimaryKeyType> {

    private final HashMap<Trip, List<ModelType>> mPerTripCache = new HashMap<>();
    private final SelectionBackedDatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>, Trip> mSelectionBackedDatabaseAdapter;
    private final String mTripForeignKeyReferenceColumnName;
    private final OrderBy mOrderBy;

    public TripForeignKeyAbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper,
                                          @NonNull String tableName,
                                          @NonNull SelectionBackedDatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>, Trip> databaseAdapter,
                                          @NonNull PrimaryKey<ModelType, PrimaryKeyType> primaryKey,
                                          @NonNull String tripForeignKeyReferenceColumnName,
                                          @NonNull OrderBy orderBy) {
        super(sqLiteOpenHelper, tableName, databaseAdapter, primaryKey, orderBy);
        mSelectionBackedDatabaseAdapter = Preconditions.checkNotNull(databaseAdapter);
        mTripForeignKeyReferenceColumnName = Preconditions.checkNotNull(tripForeignKeyReferenceColumnName);
        mOrderBy = Preconditions.checkNotNull(orderBy);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @return a {@link Single} with: all objects assigned to this foreign key in descending order
     */
    @NonNull
    public Single<List<ModelType>> get(@NonNull Trip trip) {
        return get(trip, true);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     * @return a {@link Single} with: all objects assigned to this foreign key in the desired order
     */
    @NonNull
    public synchronized Single<List<ModelType>> get(@NonNull final Trip trip, final  boolean isDescending) {
        return Single.fromCallable(() -> TripForeignKeyAbstractSqlTable.this.getBlocking(trip, isDescending));
    }

    /**
     * We fetch the un-synced receipts for a given parent {@link Trip}, since our receipt "index" is
     * rather hacky (ie programtic) instead of database driven via a sorting order. As this is dependent
     * on the specific order of the receipt in the trip itself, we must fetch this for a parent trip
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param syncProvider the provided {@link SyncProvider}
     * @return a {@link Single} with: all unsycned objects assigned to this foreign key in the desired order
     */
    @NonNull
    public synchronized Single<List<ModelType>> getUnsynced(@NonNull final Trip trip, @NonNull final SyncProvider syncProvider) {
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Google Drive is the only supported provider at the moment");

        return get(trip)
                .flatMap(getResults -> {
                    final List<ModelType> unsyncedGetResults = new ArrayList<>(getResults.size());
                    for (final ModelType model : getResults) {
                        if (model instanceof Syncable) {
                            final Syncable syncable = (Syncable) model;
                            if (!syncable.getSyncState().isSynced(syncProvider)) {
                                unsyncedGetResults.add(model);
                            }
                        }
                    }
                    return Single.just(unsyncedGetResults);
                });

    }

    @NonNull
    public synchronized List<ModelType> getBlocking(@NonNull Trip trip, boolean isDescending) {
        // We only cache descending entries
        final boolean cacheResults = isDescending;

        if (mPerTripCache.containsKey(trip) && cacheResults) {
            return new ArrayList<>(mPerTripCache.get(trip));
        }

        Cursor cursor = null;
        try {
            final List<ModelType> results = new ArrayList<>();
            cursor = getReadableDatabase().query(getTableName(), null, mTripForeignKeyReferenceColumnName + "= ? AND "
                    + COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{ Integer.toString(trip.getId()), Integer.toString(0) },
                    null, null, new OrderByColumn(mOrderBy.getOrderByColumn(), isDescending).getOrderByPredicate());
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    results.add(mSelectionBackedDatabaseAdapter.readForSelection(cursor, trip, isDescending));
                }
                while (cursor.moveToNext());
            }
            if (cacheResults) {
                mPerTripCache.put(trip, results);
            }
            return new ArrayList<>(results);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @NonNull
    @Override
    public synchronized List<ModelType> getBlocking() {
        final List<ModelType> results = super.getBlocking();
        final HashMap<Trip, List<ModelType>> localCache = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            final ModelType modelType = results.get(i);
            final Trip trip = getTripFor(modelType);
            if (!mPerTripCache.containsKey(trip)) {
                // Note: we only populate items here that haven't been previously added to the cache
                if (localCache.containsKey(trip)) {
                    final List<ModelType> perTripResults = localCache.get(trip);
                    perTripResults.add(modelType);
                } else {
                    localCache.put(trip, new ArrayList<>(Collections.singletonList(modelType)));
                }
            }
        }
        mPerTripCache.putAll(localCache);
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized Optional<ModelType> insertBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final Optional<ModelType> insertedItem = super.insertBlocking(modelType, databaseOperationMetadata);
        if (insertedItem.isPresent()) {
            final Trip trip = getTripFor(insertedItem.get());
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.add(insertedItem.get());
                if (insertedItem.get() instanceof Comparable<?>) {
                    Collections.sort((List<? extends Comparable>)perTripResults);
                }
            }
        }
        return insertedItem;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized Optional<ModelType> updateBlocking(@NonNull ModelType oldModelType, @NonNull ModelType newModelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final Optional<ModelType> updatedItem = super.updateBlocking(oldModelType, newModelType, databaseOperationMetadata);
        if (updatedItem.isPresent()) {
            Logger.debug(this, "Successfully updated this item in our table");
            final Trip oldTrip = getTripFor(oldModelType);
            if (mPerTripCache.containsKey(oldTrip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(oldTrip);
                boolean wasCachedResultRemoved = perTripResults.remove(oldModelType);
                if (!wasCachedResultRemoved) {
                    // If our cache is wrong, let's use the actual primary key to see if we can find it
                    final PrimaryKeyType primaryKeyValue = primaryKey.getPrimaryKeyValue(newModelType);
                    Logger.debug(this, "Failed to remove {} with primary key {} from our cache. Searching through to manually remove...", newModelType.getClass(), primaryKeyValue);
                    for (final ModelType cachedResult : perTripResults) {
                        if (primaryKeyValue.equals(primaryKey.getPrimaryKeyValue(cachedResult))) {
                            wasCachedResultRemoved = perTripResults.remove(cachedResult);
                            if (wasCachedResultRemoved) {
                                break;
                            }
                        }
                    }
                    if (!wasCachedResultRemoved) {
                        Logger.warn(this, "Primary key {} was never found in our cache.", primaryKeyValue);
                    }
                } else {
                    Logger.debug(this, "Found this item in our cache during update. Removing it");
                }

            }

            boolean isMarkedForDeletion = false;
            if (updatedItem.get() instanceof Syncable) {
                final Syncable syncable = (Syncable) newModelType;
                if (syncable.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive)) {
                    isMarkedForDeletion = true;
                }
            }

            final Trip newTrip = getTripFor(updatedItem.get());
            if (!isMarkedForDeletion && mPerTripCache.containsKey(newTrip)) {
                Logger.debug(this, "This item is not marked for deletion. Adding it to our cache");
                final List<ModelType> perTripResults = mPerTripCache.get(newTrip);
                perTripResults.add(updatedItem.get());
                if (updatedItem.get() instanceof Comparable<?>) {
                    Collections.sort((List<? extends Comparable>)perTripResults);
                }
            }
        }
        return updatedItem;
    }

    public synchronized void updateParentBlocking(@NonNull Trip oldTrip, @NonNull Trip newTrip) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(mTripForeignKeyReferenceColumnName, newTrip.getId());
        getWritableDatabase().update(getTableName(), contentValues, mTripForeignKeyReferenceColumnName + "= ?",
                new String[]{ Integer.toString(oldTrip.getId()) });
        mPerTripCache.remove(oldTrip);
    }

    @Override
    public synchronized Optional<ModelType> deleteBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final Optional<ModelType> deleteResult = super.deleteBlocking(modelType, databaseOperationMetadata);
        if (deleteResult.isPresent()) {
            final Trip trip = getTripFor(modelType);
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.remove(modelType);
            }
        }
        return deleteResult;
    }

    public synchronized void deleteParentBlocking(@NonNull Trip trip) {
        getWritableDatabase().delete(getTableName(), mTripForeignKeyReferenceColumnName + "= ?", new String[]{ Integer.toString(trip.getId()) });
        mPerTripCache.remove(trip);
    }

    @Override
    public synchronized boolean deleteSyncDataBlocking(@NonNull SyncProvider syncProvider) {
        final boolean success = super.deleteSyncDataBlocking(syncProvider);
        if (success) {
            // Clear out our cached data, so we're not out of sync
            mPerTripCache.clear();
        }
        return success;
    }

    @NonNull
    public synchronized Optional<ModelType> findByPrimaryKeyBlocking(@NonNull PrimaryKeyType primaryKeyType) {
        for (final Map.Entry<Trip, List<ModelType>> tripListEntry : mPerTripCache.entrySet()) {
            for (final ModelType cachedResult : tripListEntry.getValue()) {
                if (primaryKey.getPrimaryKeyValue(cachedResult).equals(primaryKeyType)) {
                    return Optional.of(cachedResult);
                }
            }
        }

        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(getTableName(), null, primaryKey.getPrimaryKeyColumn() + " = ? AND " + COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{primaryKeyType.toString(), Integer.toString(0)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final ModelType foundModel = databaseAdapter.read(cursor);
                final List<ModelType> cachedResultsList = getBlocking(getTripFor(foundModel), true); // Note: We do this b/c of issues with the receipt index field
                for (final ModelType cachedResult : cachedResultsList) {
                    if (primaryKey.getPrimaryKeyValue(cachedResult).equals(primaryKeyType)) {
                        return Optional.of(cachedResult);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return Optional.absent();
    }

    @Override
    public synchronized void clearCache() {
        super.clearCache();
        mPerTripCache.clear();
    }

    /**
     * Gets the parent {@link Trip} for this {@link ModelType} instance
     *
     * @param modelType the {@link ModelType} to get the trip for
     * @return the parent {@link Trip} instance
     */
    @NonNull
    protected abstract Trip getTripFor(@NonNull ModelType modelType);
}
