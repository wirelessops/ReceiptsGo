package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Column} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Column} objects
 */
public final class ColumnBuilderFactory<T> implements BuilderFactory<Column<T>> {

    private final ColumnDefinitions<T> columnDefinitions;
    private int id;
    private int columnType;
    private SyncState syncState;
    private long customOrderId;

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
        id = Column.UNKNOWN_ID;
        columnType = 0;
        syncState = new DefaultSyncState();
        customOrderId = 0;
    }

    public ColumnBuilderFactory<T> setColumnId(int id) {
        this.id = id;
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
        return columnDefinitions.getColumn(id, columnType, syncState, customOrderId);
    }

}
