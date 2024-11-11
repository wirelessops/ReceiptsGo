package com.wops.receiptsgo.model.impl.columns.distance

import androidx.annotation.StringRes
import com.wops.receiptsgo.R
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.*
import com.wops.receiptsgo.model.impl.columns.AbstractColumnImpl
import com.wops.receiptsgo.model.impl.columns.distance.DistanceColumnDefinitions.ActualDefinition.*
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.core.sync.model.SyncState
import com.wops.core.sync.model.impl.DefaultSyncState
import com.wops.receiptsgo.workers.reports.ReportResourcesManager
import java.util.*

/**
 * Provides specific definitions for all [Distance] [Column] objects
 */
class DistanceColumnDefinitions(
    private val reportResourcesManager: ReportResourcesManager,
    private val preferences: UserPreferenceManager,
    private val dateFormatter: DateFormatter,
    private val allowSpecialCharacters: Boolean
) : ColumnDefinitions<Distance> {
    private val actualDefinitions = values()

    /**
     * Note: Column types must be unique
     * Column type must be >= 0
     */
    internal enum class ActualDefinition(
        override val columnType: Int,
        @StringRes override val columnHeaderId: Int
    ) : ActualColumnDefinition {
        LOCATION(0, R.string.distance_location_field),
        PRICE(1, R.string.distance_price_field),
        DISTANCE(2, R.string.distance_distance_field),
        CURRENCY(3, R.string.dialog_currency_field),
        RATE(4, R.string.distance_rate_field),
        DATE(5, R.string.distance_date_field),
        COMMENT(6, R.string.distance_comment_field);
    }


    override fun getColumn(
        id: Int,
        columnType: Int,
        syncState: SyncState,
        ignoredCustomOrderId: Long,
        ignoredUUID: UUID
    ): Column<Distance> {
        for (definition in actualDefinitions) {
            if (columnType == definition.columnType) {
                return getColumnFromClass(definition, id, syncState)
            }
        }
        throw IllegalArgumentException("Unknown column type: $columnType")
    }

    override fun getAllColumns(): List<Column<Distance>> {
        val columns = ArrayList<AbstractColumnImpl<Distance>>(actualDefinitions.size)
        for (definition in actualDefinitions) {
            columns.add(getColumnFromClass(definition))
        }
        return ArrayList<Column<Distance>>(columns)
    }

    override fun getDefaultInsertColumn(): Column<Distance> {
        // Hack for the distance default until we let users dynamically set columns. Actually, this will never be called
        return getColumnFromClass(DISTANCE)
    }


    private fun getColumnFromClass(
        definition: ActualDefinition,
        id: Int = Keyed.MISSING_ID,
        syncState: SyncState = DefaultSyncState()
    ): AbstractColumnImpl<Distance> {
        val localizedContext = reportResourcesManager.getLocalizedContext()

        return when (definition) {
            LOCATION -> DistanceLocationColumn(id, syncState, localizedContext)
            PRICE -> DistancePriceColumn(id, syncState, allowSpecialCharacters)
            DISTANCE -> DistanceDistanceColumn(id, syncState)
            CURRENCY -> DistanceCurrencyColumn(id, syncState)
            RATE -> DistanceRateColumn(id, syncState)
            DATE -> DistanceDateColumn(id, syncState, dateFormatter)
            COMMENT -> DistanceCommentColumn(id, syncState)
        }
    }
}
