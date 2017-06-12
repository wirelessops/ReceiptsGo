package co.smartreceipts.android.model.impl.columns.categories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;


public class CategoryCodeColumn extends AbstractColumnImpl<SumCategoryGroupingResult> {

    public CategoryCodeColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    @Nullable
    @Override
    public String getValue(@NonNull SumCategoryGroupingResult sumCategoryGroupingResult) {
        return sumCategoryGroupingResult.getCategory().getCode();
    }
}
