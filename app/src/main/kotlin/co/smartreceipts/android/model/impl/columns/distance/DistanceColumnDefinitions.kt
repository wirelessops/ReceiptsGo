package co.smartreceipts.android.model.impl.columns.distance

import android.support.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions.ActualDefinition.*
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import java.util.*

/**
 * Provides specific definitions for all [co.smartreceipts.android.model.Receipt] [co.smartreceipts.android.model.Column]
 * objects
 */
class DistanceColumnDefinitions(
    private val reportResourcesManager: ReportResourcesManager,
    private val preferences: UserPreferenceManager,
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
            DATE -> DistanceDateColumn(id, syncState, localizedContext, preferences)
            COMMENT -> DistanceCommentColumn(id, syncState)
        }
    }
}
