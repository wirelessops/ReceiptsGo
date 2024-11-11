package com.wops.receiptsgo.model.impl.columns.categories

import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
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
