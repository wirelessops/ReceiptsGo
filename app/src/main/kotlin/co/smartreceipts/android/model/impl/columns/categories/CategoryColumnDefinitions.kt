package co.smartreceipts.android.model.impl.columns.categories

import android.support.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.model.ActualColumnDefinition
import co.smartreceipts.android.model.Column
import co.smartreceipts.android.model.ColumnDefinitions
import co.smartreceipts.android.model.comparators.ColumnNameComparator
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.impl.columns.categories.CategoryColumnDefinitions.ActualDefinition.*
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import java.util.*

class CategoryColumnDefinitions(private val reportResourcesManager: ReportResourcesManager) :
    ColumnDefinitions<SumCategoryGroupingResult> {

    private val actualDefinitions = values()

    /**
     * Note: Column types must be unique
     * Column type must be >= 0
     */
    internal enum class ActualDefinition(
        private val columnType: Int,
        private val stringResId: Int
    ) : ActualColumnDefinition {
        NAME(0, R.string.category_name_field),
        CODE(1, R.string.category_code_field),
        PRICE(2, R.string.category_price_field),
        TAX(3, R.string.category_tax_field),
        CURRENCY(4, R.string.category_currency_field);

        override fun getColumnType(): Int = columnType

        @StringRes
        override fun getColumnHeaderId(): Int = stringResId
    }

    override fun getColumn(
        id: Int,
        columnType: Int,
        syncState: SyncState,
        ignoredCustomOrderId: Long
    ): Column<SumCategoryGroupingResult> {
        for (definition in actualDefinitions) {

            if (columnType == definition.columnType) {
                return getColumnFromClass(id, definition, syncState)
            }
        }
        throw IllegalArgumentException("Unknown column type: $columnType")
    }

    override fun getAllColumns(): List<Column<SumCategoryGroupingResult>> {
        val columns =
            ArrayList<AbstractColumnImpl<SumCategoryGroupingResult>>(actualDefinitions.size)
        for (definition in actualDefinitions) {
            val column = getColumnFromClass(Column.UNKNOWN_ID, definition, DefaultSyncState())

            columns.add(column)
        }
        Collections.sort(columns, ColumnNameComparator(reportResourcesManager))
        return ArrayList<Column<SumCategoryGroupingResult>>(columns)
    }

    override fun getDefaultInsertColumn(): Column<SumCategoryGroupingResult> {
        return getColumnFromClass(Column.UNKNOWN_ID, NAME, DefaultSyncState())
    }


    private fun getColumnFromClass(
        id: Int,
        definition: ActualDefinition,
        syncState: SyncState
    ): AbstractColumnImpl<SumCategoryGroupingResult> {
        return when (definition) {
            NAME -> CategoryNameColumn(id, syncState)
            CODE -> CategoryCodeColumn(id, syncState)
            PRICE -> CategoryPriceColumn(id, syncState)
            TAX -> CategoryTaxColumn(id, syncState)
            CURRENCY -> CategoryCurrencyColumn(id, syncState)
        }
    }
}
