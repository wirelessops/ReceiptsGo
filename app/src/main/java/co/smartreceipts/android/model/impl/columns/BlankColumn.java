package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class BlankColumn<T> extends AbstractColumnImpl<T> {

    public BlankColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    public BlankColumn(int id, @NonNull String name, @NonNull SyncState syncState, long customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @NonNull
    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return "";
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<T> rows) {
        return "";
    }
}
