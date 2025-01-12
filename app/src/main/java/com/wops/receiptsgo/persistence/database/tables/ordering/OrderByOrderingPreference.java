package com.wops.receiptsgo.persistence.database.tables.ordering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.persistence.database.tables.Table;

public class OrderByOrderingPreference implements OrderBy {

    private final Class<? extends Table<?>> tableClass;
    private final OrderBy preferenceOrderedOrderBy;
    private final OrderBy fallbackOrderByIfNoPreferenceDefined;
    protected final OrderingPreferencesManager orderingPreferencesManager;

    public OrderByOrderingPreference(@NonNull OrderingPreferencesManager orderingPreferencesManager,
                                     @NonNull Class<? extends Table<?>> tableClass,
                                     @NonNull OrderBy preferenceOrderedOrderBy,
                                     @NonNull OrderBy fallbackOrderByIfNoPreferenceDefined) {
        this.orderingPreferencesManager = Preconditions.checkNotNull(orderingPreferencesManager);
        this.tableClass = Preconditions.checkNotNull(tableClass);
        this.preferenceOrderedOrderBy = Preconditions.checkNotNull(preferenceOrderedOrderBy);
        this.fallbackOrderByIfNoPreferenceDefined = Preconditions.checkNotNull(fallbackOrderByIfNoPreferenceDefined);
    }

    @Nullable
    @Override
    public String getOrderByColumn() {
        if (orderingPreferencesManager.isOrdered(tableClass)) {
            return preferenceOrderedOrderBy.getOrderByColumn();
        } else {
            return fallbackOrderByIfNoPreferenceDefined.getOrderByColumn();
        }
    }

    @Nullable
    @Override
    public final String getOrderByPredicate() {
        if (orderingPreferencesManager.isOrdered(tableClass)) {
            return preferenceOrderedOrderBy.getOrderByPredicate();
        } else {
            return fallbackOrderByIfNoPreferenceDefined.getOrderByPredicate();
        }
    }

    @Override
    @Nullable
    public String toString() {
        return getOrderByPredicate();
    }

}
