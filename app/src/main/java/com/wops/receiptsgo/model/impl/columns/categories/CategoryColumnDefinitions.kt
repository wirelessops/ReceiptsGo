package com.wops.receiptsgo.model.impl.columns.categories

import androidx.annotation.StringRes
import com.wops.receiptsgo.R
import com.wops.receiptsgo.model.ActualColumnDefinition
import com.wops.receiptsgo.model.Column
import com.wops.receiptsgo.model.ColumnDefinitions
import com.wops.receiptsgo.model.Keyed
import com.wops.receiptsgo.model.comparators.ColumnNameComparator
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.model.impl.columns.categories.CategoryColumnDefinitions.ActualDefinition.*
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.impl.DefaultSyncState
import com.wops.receiptsgo.workers.reports.ReportResourcesManager
import java.util.*

class CategoryColumnDefinitions(private val reportResourcesManager: ReportResourcesManager,
                                private val multiCurrency: Boolean,
                                private val taxEnabled: Boolean) :
    ColumnDefinitions<SumCategoryGroupingResult> {

    private val actualDefinitions = values()

    /**
     * Note: Column types must be unique
     * Column type must be >= 0
     */
    internal enum class ActualDefinition(
        override val columnType: Int,
        @StringRes override val columnHeaderId: Int
    ) : ActualColumnDefinition {
        NAME(0, R.string.category_name_field),
        CODE(1, R.string.category_code_field),
        PRICE(2, R.string.category_price_field),
        TAX(3, R.string.category_tax_field),
        PRICE_EXCHANGED(4, R.string.category_price_exchanged_field);

    }

    override fun getColumn(
        id: Int,
        columnType: Int,
        syncState: SyncState,
        ignoredCustomOrderId: Long,
        ignoredUuid: UUID
    ): Column<SumCategoryGroupingResult> {
        for (definition in actualDefinitions) {

            if (columnType == definition.columnType) {
                return getColumnFromClass(definition, id, syncState)
            }
        }
        throw IllegalArgumentException("Unknown column type: $columnType")
    }

    override fun getAllColumns(): List<Column<SumCategoryGroupingResult>> {
        val columns = ArrayList<AbstractColumnImpl<SumCategoryGroupingResult>>(actualDefinitions.size)

        for (definition in actualDefinitions) {
            if (definition == PRICE_EXCHANGED && !multiCurrency) {
                // don't include PRICE_EXCHANGED definition if all receipts have same currency
                continue
            } else if (definition == TAX && !taxEnabled) {
                // don't include TAX definition if tax field is disabled
                continue
            } else {
                columns.add(getColumnFromClass(definition))
            }
        }

        Collections.sort(columns, ColumnNameComparator(reportResourcesManager))
        return ArrayList<Column<SumCategoryGroupingResult>>(columns)
    }

    override fun getDefaultInsertColumn(): Column<SumCategoryGroupingResult> {
        return getColumnFromClass(NAME)
    }

    private fun getColumnFromClass(
        definition: ActualDefinition,
        id: Int = Keyed.MISSING_ID,
        syncState: SyncState = DefaultSyncState()
    ): AbstractColumnImpl<SumCategoryGroupingResult> {
        return when (definition) {
            NAME -> CategoryNameColumn(id, syncState)
            CODE -> CategoryCodeColumn(id, syncState)
            PRICE -> CategoryPriceColumn(id, syncState)
            TAX -> CategoryTaxColumn(id, syncState)
            PRICE_EXCHANGED -> CategoryExchangedPriceColumn(id, syncState)
        }
    }
}
