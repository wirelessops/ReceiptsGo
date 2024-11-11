package com.wops.receiptsgo.persistence.database.controllers.alterations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import io.reactivex.Single;

public class CategoriesTableActionAlterations extends StubTableActionAlterations<Category> {

    private final ReceiptsTable receiptsTable;

    public CategoriesTableActionAlterations(@NonNull ReceiptsTable receiptsTable) {
        this.receiptsTable = Preconditions.checkNotNull(receiptsTable);
    }

    @NonNull
    @Override
    public Single<Category> postUpdate(@NonNull Category oldCategory, @Nullable Category newCategory) {
        return super.postUpdate(oldCategory, newCategory)
                .doOnSuccess(ignored -> receiptsTable.clearCache());
    }

    @NonNull
    @Override
    public Single<Category> postDelete(@Nullable Category category) {
        return super.postDelete(category)
                .doOnSuccess(ignored -> receiptsTable.clearCache());
    }
}
