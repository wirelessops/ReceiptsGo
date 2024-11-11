package com.wops.receiptsgo.model.factory;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Keyed;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link com.wops.receiptsgo.model.Column} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link com.wops.receiptsgo.model.Column} objects
 */
public final class ColumnBuilderFactory<T> implements BuilderFactory<Column<T>> {

    private final ColumnDefinitions<T> columnDefinitions;
    private int id;
    private UUID uuid;
    private int columnType;
    private SyncState syncState;
    private long customOrderId;

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
        id = Keyed.MISSING_ID;
        uuid = Keyed.Companion.getMISSING_UUID();
        columnType = 0;
        syncState = new DefaultSyncState();
        customOrderId = 0;
    }

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions, @NonNull Column<T> column) {
        this.columnDefinitions = columnDefinitions;
        id = column.getId();
        uuid = column.getUuid();
        columnType = column.getType();
        syncState = column.getSyncState();
        customOrderId = column.getCustomOrderId();
    }

    public ColumnBuilderFactory<T> setColumnId(int id) {
        this.id = id;
        return this;
    }

    public ColumnBuilderFactory<T> setColumnUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ColumnBuilderFactory<T> setColumnType(int columnType) {
        this.columnType = columnType;
        return this;
    }

    public ColumnBuilderFactory<T> setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    public ColumnBuilderFactory<T> setCustomOrderId(long orderId) {
        customOrderId = orderId;
        return this;
    }

    @NonNull
    @Override
    public Column<T> build() {
        return columnDefinitions.getColumn(id, columnType, syncState, customOrderId, uuid);
    }

}
