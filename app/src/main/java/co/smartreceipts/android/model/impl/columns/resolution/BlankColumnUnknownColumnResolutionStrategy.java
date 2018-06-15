package co.smartreceipts.android.model.impl.columns.resolution;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.UnknownColumnResolutionStrategy;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * Provides a default implementation of the {@link UnknownColumnResolutionStrategy}
 * that always returns a {@link co.smartreceipts.android.model.impl.columns.BlankColumn}
 */
public final class BlankColumnUnknownColumnResolutionStrategy<T> implements UnknownColumnResolutionStrategy<T> {

    @Override
    @NonNull
    public Column<T> resolve(int id, @NonNull String columnName, @NonNull SyncState syncState, long customOrderId) {
        return new BlankColumn<>(id, columnName, syncState, customOrderId);
    }
}
