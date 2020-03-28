package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.core.sync.model.SyncState


class CategoryCodeColumn(id: Int, syncState: SyncState) :
    AbstractColumnImpl<SumCategoryGroupingResult>(
        id,
        CategoryColumnDefinitions.ActualDefinition.CODE,
        syncState
    ) {

    override fun getValue(rowItem: SumCategoryGroupingResult): String =
        rowItem.category.code
}
