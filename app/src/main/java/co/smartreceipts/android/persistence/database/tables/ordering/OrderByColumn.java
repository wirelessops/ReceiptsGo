package co.smartreceipts.android.persistence.database.tables.ordering;

import androidx.annotation.Nullable;
import android.text.TextUtils;

public class OrderByColumn implements OrderBy {

    private final String mSortByColumn;
    private final boolean mIsDescending;

    public OrderByColumn(@Nullable String sortByColumn, boolean isDescending) {
        mSortByColumn = sortByColumn;
        mIsDescending = isDescending;
    }

    @Nullable
    @Override
    public String getOrderByColumn() {
        return mSortByColumn;
    }

    @Nullable
    @Override
    public final String getOrderByPredicate() {
        if (!TextUtils.isEmpty(mSortByColumn)) {
            return mSortByColumn + ((mIsDescending) ? " DESC" : " ASC");
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public String toString() {
        return getOrderByPredicate();
    }

}
