package co.smartreceipts.android.model.impl.columns.resolution;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.UnknownColumnResolutionStrategy;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.ConstantColumn;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * Provides a default implementation of the {@link UnknownColumnResolutionStrategy}
 * that always returns a {@link ConstantColumn} unless the column name is empty (in which case it
 * will return a {@link BlankColumn}
 */
public final class ConstantColumnUnknownColumnResolutionStrategy<T> implements UnknownColumnResolutionStrategy<T> {

    @NonNull
    @Override
    public Column<T> resolve(int id, @NonNull String columnName, @NonNull SyncState syncState, long customOrderId) {
        if (TextUtils.isEmpty(columnName)) {
            return new BlankColumn<>(id, columnName, syncState, customOrderId);
        } else {
            return new ConstantColumn<>(id, columnName, syncState, customOrderId);
        }
    }
}

