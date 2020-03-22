package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.adapters.SyncStateAdapter;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderBy;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderByDatabaseDefault;
import co.smartreceipts.core.sync.model.Syncable;
import co.smartreceipts.core.sync.provider.SyncProvider;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Single;

/**
 * Abstracts out the core CRUD database operations in order to ensure that each of our core table instances
 * operate in a standard manner.
 *
 * @param <ModelType>      the model object that CRUD operations here should return
 */
public abstract class AbstractSqlTable<ModelType extends Keyed & Syncable> implements Table<ModelType> {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UUID = "entity_uuid";
    public static final String COLUMN_DRIVE_SYNC_ID = "drive_sync_id";
    public static final String COLUMN_DRIVE_IS_SYNCED = "drive_is_synced";
    public static final String COLUMN_DRIVE_MARKED_FOR_DELETION = "drive_marked_for_deletion";
    public static final String COLUMN_LAST_LOCAL_MODIFICATION_TIME = "last_local_modification_time";
    public static final String COLUMN_CUSTOM_ORDER_ID = "custom_order_id";

    private final SQLiteOpenHelper sqLiteOpenHelper;
    private final String tableName;

    protected final DatabaseAdapter<ModelType> databaseAdapter;
    private final OrderBy orderBy;

    private SQLiteDatabase initialNonRecursivelyCalledDatabase;
    private List<ModelType> cachedResults;


    public AbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper,
                            @NonNull String tableName,
                            @NonNull DatabaseAdapter<ModelType> databaseAdapter) {
        this(sqLiteOpenHelper, tableName, databaseAdapter, new OrderByDatabaseDefault());
    }

    public AbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull String tableName,
                            @NonNull DatabaseAdapter<ModelType> databaseAdapter, @NonNull OrderBy orderBy) {
        this.sqLiteOpenHelper = Preconditions.checkNotNull(sqLiteOpenHelper);
        this.tableName = Preconditions.checkNotNull(tableName);
        this.databaseAdapter = Preconditions.checkNotNull(databaseAdapter);
        this.orderBy = Preconditions.checkNotNull(orderBy);
    }

    public final SQLiteDatabase getReadableDatabase() {
        if (initialNonRecursivelyCalledDatabase == null) {
            return sqLiteOpenHelper.getReadableDatabase();
        } else {
            return initialNonRecursivelyCalledDatabase;
        }
    }

    public final SQLiteDatabase getWritableDatabase() {
        if (initialNonRecursivelyCalledDatabase == null) {
            return sqLiteOpenHelper.getWritableDatabase();
        } else {
            return initialNonRecursivelyCalledDatabase;
        }
    }

    @Override
    @NonNull
    public final String getTableName() {
        return tableName;
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    protected synchronized void onUpgradeToAddSyncInformation(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 14) { // Add syncing state information
            final String alter1 = "ALTER TABLE " + getTableName() + " ADD " + COLUMN_DRIVE_SYNC_ID + " TEXT";
            final String alter2 = "ALTER TABLE " + getTableName() + " ADD " + COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0";
            final String alter3 = "ALTER TABLE " + getTableName() + " ADD " + COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0";
            final String alter4 = "ALTER TABLE " + getTableName() + " ADD " + COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE";

            db.execSQL(alter1);
            db.execSQL(alter2);
            db.execSQL(alter3);
            db.execSQL(alter4);
        }
    }

    protected synchronized void onUpgradeToAddUUID(@NonNull SQLiteDatabase db, int oldVersion) {
        if (oldVersion <= 18) { // Add a uuid to all app database tables
            final String addNewColumn = "ALTER TABLE " + getTableName() + " ADD " + COLUMN_UUID + " TEXT";
            db.execSQL(addNewColumn);

            // assign random values
            try (Cursor cursor = db.query(getTableName(), new String[]{COLUMN_ID}, null, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {

                    final int idIdx = cursor.getColumnIndex(COLUMN_ID);

                    do {
                        final int id = cursor.getInt(idIdx);
                        final String uuid = UUID.randomUUID().toString();

                        final ContentValues columnValues = new ContentValues(1);
                        columnValues.put(COLUMN_UUID, uuid);
                        Logger.debug(this, "Updating UUID value to {}", uuid);

                        if (db.update(getTableName(), columnValues, COLUMN_ID + "= ?", new String[]{Integer.toString(id)}) == 0) {
                            throw new IllegalStateException("Column update error happened");
                        }
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    @Override
    public synchronized final void onPostCreateUpgrade() {
        // We no longer need to worry about recursive database calls
        initialNonRecursivelyCalledDatabase = null;
    }

    @NonNull
    @Override
    public Single<List<ModelType>> get() {
        return Single.fromCallable(this::getBlocking);
    }

    /**
     * This method aims to fetch all entities that have been marked for deletion but are not yet delete
     *
     * @param syncProvider the provided {@link SyncProvider} to check for
     * @return a {@link Single} containing a {@link List} all items assigned for deletion
     */
    @NonNull
    public synchronized Single<List<ModelType>> getAllMarkedForDeletionItems(@NonNull final SyncProvider syncProvider) {
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Google Drive is the only supported provider at the moment");

        return Single.fromCallable(() -> {
            Cursor cursor = null;
            try {
                final List<ModelType> results = new ArrayList<>();
                cursor = getReadableDatabase().query(getTableName(), null, COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(1)}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        results.add(databaseAdapter.read(cursor));
                    }
                    while (cursor.moveToNext());
                }
                return results;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    @NonNull
    @Override
    public Single<ModelType> findByPrimaryKey(final int primaryKeyType) {
        return Single.fromCallable(() -> AbstractSqlTable.this.findByPrimaryKeyBlocking(primaryKeyType))
                .map(modelTypeOptional -> {
                    if (modelTypeOptional.isPresent()) {
                        return modelTypeOptional.get();
                    } else {
                        throw new Exception("Find by primary key failed. No such key");
                    }
                });
    }

    @NonNull
    @Override
    public final Single<ModelType> insert(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        return Single.fromCallable(() -> AbstractSqlTable.this.insertBlocking(modelType, databaseOperationMetadata))
                .map(modelTypeOptional -> {
                    if (modelTypeOptional.isPresent()) {
                        return modelTypeOptional.get();
                    } else {
                        throw new Exception("Insert failed.");
                    }
                });
    }

    @NonNull
    @Override
    public final Single<ModelType> update(@NonNull final ModelType oldModelType, @NonNull final ModelType newModelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        return Single.fromCallable(() -> AbstractSqlTable.this.updateBlocking(oldModelType, newModelType, databaseOperationMetadata))
                .map(modelTypeOptional -> {
                    if (modelTypeOptional.isPresent()) {
                        return modelTypeOptional.get();
                    } else {
                        throw new Exception("Update failed.");
                    }
                });
    }

    @NonNull
    @Override
    public final Single<ModelType> delete(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        return Single.fromCallable(() -> AbstractSqlTable.this.deleteBlocking(modelType, databaseOperationMetadata))
                .map(modelTypeOptional -> {
                    if (modelTypeOptional.isPresent()) {
                        return modelTypeOptional.get();
                    } else {
                        throw new Exception("Delete failed.");
                    }
                });
    }

    @NonNull
    public Single<Boolean> deleteSyncData(@NonNull final SyncProvider syncProvider) {
        return Single.fromCallable(() -> AbstractSqlTable.this.deleteSyncDataBlocking(syncProvider));
    }

    @NonNull
    public synchronized List<ModelType> getBlocking() {
        if (cachedResults != null) {
            return cachedResults;
        }

        Cursor cursor = null;
        try {
            cachedResults = new ArrayList<>();
            cursor = getReadableDatabase().query(getTableName(), null, COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(0)}, null, null, orderBy.getOrderByPredicate());
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    cachedResults.add(databaseAdapter.read(cursor));
                }
                while (cursor.moveToNext());
            }
            return new ArrayList<>(cachedResults);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public synchronized Optional<ModelType> insertBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = databaseAdapter.write(modelType, databaseOperationMetadata);

        // to be sure that entity_uuid is not missed
        if (!values.containsKey(COLUMN_UUID) || UUID.fromString(values.getAsString(COLUMN_UUID)).equals(Keyed.Companion.getMISSING_UUID())) {
            Logger.warn(this, "Assigning random UUID to new model before inserting");
            values.put(COLUMN_UUID, UUID.randomUUID().toString());
        }
        UUID uuid = UUID.fromString(values.getAsString(COLUMN_UUID));

        if (getWritableDatabase().insertOrThrow(getTableName(), null, values) != -1) {
            try (Cursor cursor = getReadableDatabase().rawQuery("SELECT last_insert_rowid()", null)) {

                final int id;
                if (cursor != null && cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                    id = cursor.getInt(0);
                } else {
                    id = -1;
                }

                final ModelType insertedItem = databaseAdapter.build(modelType, id, uuid, databaseOperationMetadata);
                if (cachedResults != null) {
                    cachedResults.add(insertedItem);
                    if (insertedItem instanceof Comparable<?>) {
                        Collections.sort((List<? extends Comparable>) cachedResults);
                    }
                }
                return Optional.of(insertedItem);
            } // Close the cursor and db to avoid memory leaks
        } else {
            return Optional.absent();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized Optional<ModelType> updateBlocking(@NonNull ModelType oldModelType, @NonNull ModelType newModelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

        final ContentValues values = databaseAdapter.write(newModelType, databaseOperationMetadata);

        // to be sure that entity_uuid will never be changed
        if (values.containsKey(COLUMN_UUID)) {
            Logger.warn(this, "Removing UUID to avoid influence on the existing value");
            values.remove(COLUMN_UUID);
        }

        final boolean updateSuccess;
        final String oldPrimaryKeyValue = String.valueOf(oldModelType.getId());

        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            // For sync operations, ensure that this only succeeds if we haven't already updated this item more recently
            final Syncable syncableOldModel = oldModelType;
            updateSuccess = getWritableDatabase().update(getTableName(), values, COLUMN_ID +
                            " = ? AND " + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " >= ?",
                    new String[]{oldPrimaryKeyValue, Long.toString(syncableOldModel.getSyncState().getLastLocalModificationTime().getTime())}) > 0;
        } else {
            updateSuccess = getWritableDatabase().update(getTableName(), values, COLUMN_ID + " = ?",
                    new String[]{oldPrimaryKeyValue}) > 0;
        }

        if (updateSuccess) {
            final ModelType updatedItem;

                // ensure we're re-using the same id as the old key and the same uuid
                updatedItem = databaseAdapter.build(newModelType, oldModelType.getId(), oldModelType.getUuid(), databaseOperationMetadata);
            if (cachedResults != null) {
                boolean wasCachedResultRemoved = cachedResults.remove(oldModelType);
                if (!wasCachedResultRemoved) {
                    // If our cache is wrong, let's use the actual primary key to see if we can find it
                    final int primaryKeyValue = newModelType.getId();
                    Logger.debug(this, "Failed to remove {} with primary key {} from our cache. Searching through to manually remove...", newModelType.getClass(), primaryKeyValue);
                    for (final ModelType cachedResult : cachedResults) {
                        if (primaryKeyValue == cachedResult.getId()) {
                            wasCachedResultRemoved = cachedResults.remove(cachedResult);
                            if (wasCachedResultRemoved) {
                                break;
                            }
                        }
                    }
                    if (!wasCachedResultRemoved) {
                        Logger.warn(this, "Primary key {} was never found in our cache.", primaryKeyValue);
                    }
                }

                if (!newModelType.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive)) {
                    cachedResults.add(updatedItem);
                }

                if (updatedItem instanceof Comparable<?>) {
                    Collections.sort((List<? extends Comparable>) cachedResults);
                }
            }
            return Optional.of(updatedItem);

        } else {
            return Optional.absent();
        }
    }

    public synchronized Optional<ModelType> deleteBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final String primaryKeyValue = String.valueOf(modelType.getId());
        if (getWritableDatabase().delete(getTableName(), COLUMN_ID + " = ?", new String[]{primaryKeyValue}) > 0) {
            if (cachedResults != null) {
                cachedResults.remove(modelType);
            }
            return Optional.of(modelType);
        } else {
            return Optional.absent();
        }
    }

    public synchronized boolean deleteSyncDataBlocking(@NonNull SyncProvider syncProvider) {
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Google Drive is the only supported provider at the moment");

        // First - remove all that are marked for deletion but haven't been actually deleted
        getWritableDatabase().delete(getTableName(), COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{Integer.toString(1)});

        // Next - update all items that currently contain sync data (to remove it)
        final ContentValues contentValues = new SyncStateAdapter().deleteSyncData(syncProvider);
        getWritableDatabase().update(getTableName(), contentValues, null, null);

        // Lastly - let's clear out all cached data
        if (cachedResults != null) {
            cachedResults.clear();
        }

        return true;
    }

    @NonNull
    public synchronized Optional<ModelType> findByPrimaryKeyBlocking(int primaryKeyValue) {
        if (cachedResults != null) {
            for (final ModelType cachedResult : cachedResults) {
                if (cachedResult.getId() == primaryKeyValue) {
                    return Optional.of(cachedResult);
                }
            }
            return Optional.absent();
        } else {
            final List<ModelType> entries = new ArrayList<>(getBlocking());
            final int size = entries.size();
            for (int i = 0; i < size; i++) {
                final ModelType modelType = entries.get(i);
                if (modelType.getId() == primaryKeyValue) {
                    return Optional.of(modelType);
                }
            }
            return Optional.absent();
        }
    }

    @Override
    public synchronized void deleteAllTableRowsBlocking() {
        getWritableDatabase().execSQL("DELETE FROM " + getTableName());
        clearCache();
    }

    @Override
    public synchronized void clearCache() {
        if (cachedResults != null) {
            cachedResults.clear();
            cachedResults = null;
        }
    }

}
