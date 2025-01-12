package com.wops.receiptsgo.model;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.UUID;

import com.wops.core.sync.model.SyncState;

/**
 * Defines a simple contract for tracking the different column types that are available to users of the app
 */
public interface ColumnDefinitions<T> {

    /**
     * Gets a {@link Column} instance for a specific column type (i.e. column name).
     *
     * @param id            the unique identifier for the column
     * @param columnType    the column type
     * @param syncState     the current {@link SyncState} of the column
     * @param customOrderId the order id for this column
     * @param uuid          the uuid for the column
     * @return a new column instance or {@code null} if none can be found
     */
    @NonNull
    Column<T> getColumn(int id, int columnType, @NonNull SyncState syncState, long customOrderId, @NonNull UUID uuid);

    /**
     * Gets a list of all {@link Column} instances that are available as part
     * of this set of definitions
     *
     * @return the list of all columns
     */
    @NonNull
    List<Column<T>> getAllColumns();

    /**
     * Gets a {@link Column} instance that can be treated as the default
     * one for this particular set of definitions when a user adds a new column
     *
     * @return a new instance of the default column
     */
    @NonNull
    Column<T> getDefaultInsertColumn();

}
