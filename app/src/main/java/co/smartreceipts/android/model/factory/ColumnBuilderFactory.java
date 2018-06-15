package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.UnknownColumnResolutionStrategy;
import co.smartreceipts.android.model.impl.columns.resolution.ConstantColumnUnknownColumnResolutionStrategy;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Column} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Column} objects
 */
public final class ColumnBuilderFactory<T> implements BuilderFactory<Column<T>> {

    private final ColumnDefinitions<T> mColumnDefinitions;
    private final UnknownColumnResolutionStrategy<T> mUnknownColumnResolutionStrategy;
    private int mId;
    private String mColumnName;
    private SyncState mSyncState;
    private long mCustomOrderId;

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions) {
        this(columnDefinitions, new ConstantColumnUnknownColumnResolutionStrategy<T>());
    }

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions, @NonNull UnknownColumnResolutionStrategy<T> unknownColumnResolutionStrategy) {
        mColumnDefinitions = columnDefinitions;
        mUnknownColumnResolutionStrategy = unknownColumnResolutionStrategy;
        mId = Column.UNKNOWN_ID;
        mColumnName = "";
        mSyncState = new DefaultSyncState();
        mCustomOrderId = 0;
    }

    public ColumnBuilderFactory<T> setColumnId(int id) {
        mId = id;
        return this;
    }

    public ColumnBuilderFactory<T> setColumnName(@Nullable Column<T> column) {
        mColumnName = (column != null) ? column.getName() : "";
        return this;
    }

    public ColumnBuilderFactory<T> setColumnName(@Nullable String name) {
        mColumnName = (name != null) ? name : "";
        return this;
    }

    public ColumnBuilderFactory<T> setSyncState(@NonNull SyncState syncState) {
        mSyncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    public ColumnBuilderFactory<T> setCustomOrderId(long orderId) {
        mCustomOrderId = orderId;
        return this;
    }

    @NonNull
    @Override
    public Column<T> build() {
        final Column<T> column = mColumnDefinitions.getColumn(mId, mColumnName, mSyncState, mCustomOrderId);
        return (column != null) ? column : mUnknownColumnResolutionStrategy.resolve(mId, mColumnName, mSyncState, mCustomOrderId);
    }

}
