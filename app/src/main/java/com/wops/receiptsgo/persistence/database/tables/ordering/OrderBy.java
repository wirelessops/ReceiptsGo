package com.wops.receiptsgo.persistence.database.tables.ordering;

import androidx.annotation.Nullable;

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
