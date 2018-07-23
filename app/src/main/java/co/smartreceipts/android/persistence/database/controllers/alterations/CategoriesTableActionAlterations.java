package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

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
