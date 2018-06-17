package co.smartreceipts.android.persistence.database.tables.ordering;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public interface OrderBy {

    /**
     * Gets the column that we attempt to order things by
     *
     * @return this column or {@code null} if we should use the default one
     */
    @Nullable
    String getOrderByColumn();

    /**
     * Generates a predicate to define the SQL OrderBy statement
     *
     * @return this predicate or {@code null} if we should use the default one
     */
    @Nullable
    String getOrderByPredicate();
}
