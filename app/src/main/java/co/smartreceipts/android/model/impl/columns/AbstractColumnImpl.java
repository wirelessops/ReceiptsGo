package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides an abstract implementation of the column contract to cover shared code
 */
public abstract class AbstractColumnImpl<T> implements Column<T> {

    private final int mId;
    private final String mName;
    private final SyncState mSyncState;
    private final int mCustomOrderId;

    public AbstractColumnImpl(int id, @NonNull String name, @NonNull SyncState syncState) {
        this(id, name, syncState, 0);
    }

    public AbstractColumnImpl(int id, @NonNull String name, @NonNull SyncState syncState, int customOrderId) {
        mId = id;
        mName = name;
        mSyncState = syncState;
        mCustomOrderId = customOrderId;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public int getCustomOrderId() {
        return mCustomOrderId;
    }

    @Override
    @NonNull
    public final String getName() {
        return mName;
    }

    @Override
    @NonNull
    public String getHeader() {
        return getName();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<T> rows) {
        return "";
    }

    @Override
    public int compareTo(@NonNull Column otherColumn) {
        return getId() - otherColumn.getId();
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return mSyncState;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractColumnImpl)) return false;

        AbstractColumnImpl that = (AbstractColumnImpl) o;

        if (mId != that.mId) return false;
        if (mCustomOrderId != that.mCustomOrderId) return false;
        if (!mName.equals(that.mName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mCustomOrderId;
        result = 31 * result + mName.hashCode();
        return result;
    }
}
