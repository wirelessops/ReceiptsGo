package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;

/**
 * Defines the primary key for the {@link co.smartreceipts.android.persistence.database.tables.CategoriesTable}
 */
public final class CategoryPrimaryKey implements PrimaryKey<Category, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return CategoriesTable.COLUMN_ID;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull Category category) {
        return category.getId();
    }
}
