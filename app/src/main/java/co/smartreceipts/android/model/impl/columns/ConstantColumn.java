package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns a constant value for all fields
 */
public final class ConstantColumn<T> extends AbstractColumnImpl<T> {

    @Deprecated
    public ConstantColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    public ConstantColumn(int id, @NonNull String name, @NonNull SyncState syncState, long customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return getName();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<T> rows) {
        return getName();
    }
}

